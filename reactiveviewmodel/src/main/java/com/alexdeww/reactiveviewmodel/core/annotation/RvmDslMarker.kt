package com.alexdeww.reactiveviewmodel.core.annotation

@DslMarker
@Target(
    AnnotationTarget.CLASS,
    AnnotationTarget.TYPEALIAS,
    AnnotationTarget.TYPE,
    AnnotationTarget.FUNCTION
)
annotation class RvmDslMarker
