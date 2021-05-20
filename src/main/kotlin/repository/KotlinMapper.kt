package repository

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import org.skife.jdbi.v2.StatementContext
import org.skife.jdbi.v2.tweak.ResultSetMapper
import org.slf4j.LoggerFactory
import java.sql.ResultSet
import kotlin.reflect.KParameter
import kotlin.reflect.full.primaryConstructor
import kotlin.reflect.jvm.isAccessible
import kotlin.reflect.jvm.javaType

open class KotlinMapper<C : Any> constructor(private val clazz: Class<C>) : ResultSetMapper<C> {
    companion object {
        val mapper = ObjectMapper().registerKotlinModule()
        private val logger = LoggerFactory.getLogger(KotlinMapper::class.java)
    }

    private val klass = clazz.kotlin

    override fun map(index: Int, rs: ResultSet, ctx: StatementContext): C {
        val constructor = klass.primaryConstructor!!
        constructor.isAccessible = true

        // TODO: best fit for constructors + writeable properties, pay attention to nullables/optionals with default values
        //       for now just call primary constructor using named params and hope

        val validParametersByName = constructor.parameters
            .filter { it.kind == KParameter.Kind.VALUE && it.name != null }
            .map { it.name!!.toLowerCase() to it }
            .toMap()

        val matchingParms: MutableList<Pair<KParameter, Any?>> = mutableListOf()
        for (i: Int in 1..rs.metaData.columnCount) {
            rs.metaData.getColumnLabel(i).replace("_", "").toLowerCase()
                .let { colLabel ->
                    logger.trace("colLabel: $colLabel")
                    validParametersByName[colLabel]
                        ?.let { param: KParameter ->
                            if (param.type.isMarkedNullable && rs.getObject(i) == null) {
                                matchingParms.add(Pair(param, null))
                            } else {
                                logger.trace("param: $param :: javaType: ${param.type.javaType}")
                                val paramType = param.type.javaType
                                when (paramType) {
                                    Boolean::class.java -> {
                                        logger.trace("found Boolean")
                                        matchingParms.add(Pair(param, rs.getBoolean(i)))
                                    }
                                    String::class.java -> {
                                        logger.trace("found String")
                                        matchingParms.add(Pair(param, rs.getString(i)))
                                    }
                                    java.lang.Integer::class.java,
                                    Int::class.java -> {
                                        logger.trace("found Integer")
                                        matchingParms.add(Pair(param, rs.getInt(i)))
                                    }
                                    java.lang.Long::class.java,
                                    Long::class.java -> {
                                        logger.trace("found Long")
                                        val value = rs.getObject(i)
                                        matchingParms.add(Pair(param, rs.getLong(i)))
                                    }

                                    java.lang.Double::class.java,
                                    Double::class.java -> {
                                        logger.trace("found Double")
                                        matchingParms.add(Pair(param, rs.getDouble(i)))
                                    }
                                    java.lang.Float::class.java,
                                    Float::class.java -> {
                                        logger.trace("found Float")
                                        matchingParms.add(Pair(param, rs.getFloat(i)))
                                    }
                                    else -> {
                                        val value = rs.getString(i)
                                        if ((param.type.javaType as Class<*>).isEnum) {
                                            logger.trace("found Enum")
                                            val enumClass = (param.type.javaType as Class<*>)
                                            matchingParms.add(Pair(param, enumClass.getMethod("valueOf", String::class.java).invoke(null, value)))
                                        } else {
                                            matchingParms.add(Pair(param, mapper.readValue(value, Class.forName(paramType.typeName))))
                                        }
                                    }
                                }
                            }
                        }
                }
        }

        val parmsThatArePresent = matchingParms.map { it.first }.toHashSet()

        // things missing from the result set that are Nullable and not optional should be set to Null
        val nullablesThatAreAbsent = constructor.parameters.filter { !it.isOptional && it.type.isMarkedNullable && it !in parmsThatArePresent }.map {
            Pair(it, null)
        }

        // things that are missing from the result set but are defaultable
        val defaultableThatAreAbsent = constructor.parameters.filter { it.isOptional && !it.type.isMarkedNullable && it !in parmsThatArePresent }.toSet()

        val finalParms = (matchingParms + nullablesThatAreAbsent)
            .filterNot { it.first in defaultableThatAreAbsent }
            .toMap()
        return constructor.callBy(finalParms)
    }

}
