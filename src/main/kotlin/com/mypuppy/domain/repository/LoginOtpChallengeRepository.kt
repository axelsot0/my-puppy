package com.mypuppy.domain.repository

import com.mypuppy.domain.model.LoginOtpChallenge
import io.quarkus.hibernate.orm.panache.kotlin.PanacheRepositoryBase
import java.util.UUID

interface LoginOtpChallengeRepository : PanacheRepositoryBase<LoginOtpChallenge, UUID>
