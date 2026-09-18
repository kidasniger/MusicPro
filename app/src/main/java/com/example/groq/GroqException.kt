package com.example.groq

sealed class GroqException(message: String, cause: Throwable? = null) : Exception(message, cause) {

    /**
     * Clé API Groq manquante, expirée ou invalide (HTTP 401).
     */
    class InvalidApiKeyException(
        message: String = "Clé API Groq invalide ou absente. Veuillez la renseigner dans les Paramètres."
    ) : GroqException(message)

    /**
     * Quota de requêtes ou limite de débit dépassé sur Groq (HTTP 429 Too Many Requests).
     */
    class RateLimitExceededException(
        message: String = "Quota dépassé sur l'API Groq (Rate limit). Veuillez patienter quelques instants avant de réessayer."
    ) : GroqException(message)

    /**
     * Fichier audio dépassant la limite de taille directe (25 Mo) ou non découpable (HTTP 413).
     */
    class FileTooLargeException(
        message: String = "Le fichier audio dépasse la limite autorisée par l'API Groq (25 Mo)."
    ) : GroqException(message)

    /**
     * Durée audio excessive ou fichier trop long pour une seule session.
     */
    class AudioTooLongException(
        message: String = "Le fichier audio est trop long pour être transcrit en une seule fois."
    ) : GroqException(message)

    /**
     * Erreur de connexion ou coupure réseau.
     */
    class NetworkException(
        message: String = "Erreur de connexion réseau : impossible de joindre les serveurs de Groq.",
        cause: Throwable? = null
    ) : GroqException(message, cause)

    /**
     * Erreur interne de serveur Groq (HTTP 5xx).
     */
    class ServerException(
        val code: Int,
        message: String = "Erreur serveur Groq ($code)."
    ) : GroqException(message)

    /**
     * Erreur générique.
     */
    class GeneralException(
        message: String,
        cause: Throwable? = null
    ) : GroqException(message, cause)
}
