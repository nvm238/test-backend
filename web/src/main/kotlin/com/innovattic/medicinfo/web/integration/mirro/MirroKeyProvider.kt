package com.innovattic.medicinfo.web.integration.mirro

import com.auth0.jwt.interfaces.RSAKeyProvider
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.security.KeyFactory
import java.security.interfaces.RSAPrivateKey
import java.security.interfaces.RSAPublicKey
import java.security.spec.PKCS8EncodedKeySpec
import java.security.spec.X509EncodedKeySpec
import java.util.*

@Component
class MirroKeyProvider(
    @Value("\${mirro.jwt.private-key}") val privateKey: String,
    @Value("\${mirro.jwt.public-key}") val publicKey: String,
) : RSAKeyProvider {

    private val rsaPublicKey: RSAPublicKey by lazy {
        val (decoded: ByteArray, keyFactory: KeyFactory) = getKeyFactory(publicKey)
        val keySpec = X509EncodedKeySpec(decoded)

        keyFactory.generatePublic(keySpec) as RSAPublicKey
    }

    private val rsaPrivateKey: RSAPrivateKey by lazy {
        val (decoded: ByteArray, keyFactory: KeyFactory) = getKeyFactory(privateKey)
        val keySpec = PKCS8EncodedKeySpec(decoded)

        keyFactory.generatePrivate(keySpec) as RSAPrivateKey
    }

    private fun getKeyFactory(key: String): Pair<ByteArray, KeyFactory> {
        val decoded: ByteArray = Base64.getDecoder().decode(key)
        val keyFactory: KeyFactory = KeyFactory.getInstance("RSA")

        return Pair(decoded, keyFactory)
    }

    override fun getPublicKeyById(keyId: String): RSAPublicKey = rsaPublicKey
    override fun getPrivateKey(): RSAPrivateKey = rsaPrivateKey
    override fun getPrivateKeyId(): String = "0"
}
