package com.hanna2100.cleannote.business.domain.util

interface EntityMapper <Entity, DomainModel> {
    fun mapFromEntity(entity: Entity): DomainModel
    fun mapToEntity(domainModel: DomainModel): Entity
}
