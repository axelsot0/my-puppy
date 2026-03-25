package com.mypuppy.domain.exception

abstract class DomainException(message: String) : RuntimeException(message)

class NotFoundException(entity: String, id: Any) :
    DomainException("$entity with id '$id' not found")

class DuplicateException(message: String) :
    DomainException(message)

class SlotNotAvailableException(message: String) :
    DomainException(message)

class SlotOverlapException(message: String) :
    DomainException(message)

class UnauthorizedException(message: String) :
    DomainException(message)

class InvalidOperationException(message: String) :
    DomainException(message)
