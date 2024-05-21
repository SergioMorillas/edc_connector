@Throws(Exception::class)
    private fun getSSLCertificate(
        ipAddressOrDNS: String,
        port: Int = 443
    ): Array<Certificate> {
        var url = "https://$ipAddressOrDNS:$port"
        return try {
            val trustAllCerts: Array<TrustManager> =
                arrayOf(
                    object : X509TrustManager {
                        override fun checkClientTrusted(
                            chain: Array<out java.security.cert.X509Certificate>?,
                            authType: String?
                        ) {}

                        override fun checkServerTrusted(
                            chain: Array<out java.security.cert.X509Certificate>?,
                            authType: String?
                        ) {}

                        override fun getAcceptedIssuers(): Array<java.security.cert.X509Certificate> = arrayOf()
                    }
                )

            val sslContext: SSLContext = SSLContext.getInstance("SSL")
            sslContext.init(null, trustAllCerts, java.security.SecureRandom())

            HttpsURLConnection.setDefaultSSLSocketFactory(sslContext.socketFactory)

            val urlObj = URL(url)
            val connection = urlObj.openConnection() as HttpsURLConnection
            connection.hostnameVerifier = HostnameVerifier { _, _ -> true }
            connection.connect()

            val certs = connection.serverCertificates
            connection.disconnect()

            if (certs == null) {
                throw Exception("ARSYS - getSSLCertificate - There is no certification array")
            }
            certs
        } catch (e: Exception) {
            throw e
        }
    }