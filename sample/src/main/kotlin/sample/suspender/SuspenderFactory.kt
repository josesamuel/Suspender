package sample.suspender

import suspender.annotations.Suspender

/**
 * A marker interface to mark which classes needs to be wrapped by [Suspender]
 *
 * Specify all the classes that needs to be wrapped
 */
@Suspender(classesToWrap = [
    Office::class,
    School::class
])
interface SuspenderFactory