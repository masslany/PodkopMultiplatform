package pl.masslany.podkop.testsupport.fakes

fun <T> notUsed(): Result<T> = error("This fake method is not used in the current test")
