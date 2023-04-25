package com.innovattic.medicinfo.file

import com.innovattic.common.file.LocalFileService
import com.innovattic.common.file.S3FileService
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile

@Configuration
open class FileConfig {
    @Bean @Profile("aws")
    open fun s3FileService(@Value("\${medicinfo.s3.bucket:}") bucketName: String) = S3FileService(bucketName)

    @Bean @Profile("!aws")
    open fun localFileService(@Value("\${medicinfo.files.folder}") folder: String) = LocalFileService(folder)
}
