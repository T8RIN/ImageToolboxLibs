package io.rektplorer64.pickerkt.builder.query.operator

enum class Operator(val sqlOperator: String) {
    Equal(sqlOperator = "="),
    NotEqual(sqlOperator = "!="),
    In(sqlOperator = "IN"),
    NotIn(sqlOperator = "NOT IN"),
    Like(sqlOperator = "LIKE"),
    NotLike(sqlOperator = "NOT LIKE"),
    GreaterThan(sqlOperator = ">"),
    GreaterThanOrEquals(sqlOperator = ">="),
    LessThan(sqlOperator = "<"),
    LessThanOrEquals(sqlOperator = "<="),
    And(sqlOperator = "AND"),
    Or(sqlOperator = "OR")
}