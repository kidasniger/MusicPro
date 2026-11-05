---
name: "MusicPro"
slug: "musicpro"
version: "1.0-b240"
status: "Stable"
category: "Lecteur musical"
author: "kidasniger"
license: "Non spécifiée dans le dépôt"
platforms:
  - "Android"
technologies:
  - "Kotlin"
  - "Jetpack Compose"
  - "Material Design 3"
  - "Coroutines/Flow"
  - "Room"
  - "Media3"
  - "Coil"
  - "Retrofit"
  - "Moshi"
  - "Jetpack Glance"
  - "Firebase"
featured: false
github: "https://github.com/kidasniger/MusicPro"
demo: "Non publiée"
website: "https://musicpro.vercel.app/"
documentation: "Non publiée"
download: "https://github.com/kidasniger/MusicPro/releases/download/v1.0-b240/MusicPro-v1.0-b240.apk"
icon: "https://raw.githubusercontent.com/kidasniger/MusicPro/main/app/src/main/res/drawable/musicpro_logo_square.png"
cover: "Non publiée"
---

# Description

MusicPro est un lecteur musical Android moderne conçu pour écouter, organiser et retrouver facilement sa musique. L'application permet de gérer une bibliothèque audio, rechercher des morceaux, créer des playlists et des favoris, lire la musique en arrière-plan, afficher des paroles synchronisées et utiliser un mode karaoké ainsi qu'un égaliseur.

MusicPro propose également un contrôle précis de la lecture avec une progression interactive sur les écrans Lecture et Paroles. Les paroles suivent la position de lecture afin de faciliter la navigation dans les morceaux et l'écoute avec affichage synchronisé.

L'interface a été enrichie avec une navigation persistante, des transitions animées, une présentation adaptative aux différentes tailles d'écran et des réglages d'accessibilité renforcés. L'application reste centrée sur la bibliothèque musicale locale.

# Description courte

Lecteur musical Android pour organiser sa bibliothèque, créer des playlists, retrouver ses morceaux, afficher des paroles synchronisées et contrôler précisément la lecture.

# Fonctionnalités

- Détection et organisation des morceaux audio présents sur l'appareil
- Recherche par titre, artiste, album ou playlist
- Historique de recherche, suggestions et filtres de bibliothèque
- Playlists et favoris personnalisés
- Lecture, pause, précédent, suivant, répétition et lecture aléatoire
- File de lecture et lecture en arrière-plan
- Notification multimédia avec commandes de lecture
- Widget de contrôle depuis l'écran d'accueil et raccourcis d'application
- Barre de progression fine et interactive avec recherche dans le morceau
- Avance et retour rapides de 10 secondes
- Paroles synchronisées avec la position de lecture
- Mode karaoké
- Recherche et génération assistée de paroles synchronisées
- Import et enregistrement des paroles associées aux morceaux
- Égaliseur audio avec préréglages
- Minuterie de sommeil et sélection de la sortie audio
- Présentation du lecteur personnalisable (Pochette, Vinyle, Minimal)
- Modes d'apparence clair, sombre et automatique
- Navigation par onglets avec conservation de la position
- Transitions partagées entre les listes, albums, artistes et lecteur
- Affichage adaptatif pour téléphones, tablettes et paysage
- Réglages d'accessibilité : cibles tactiles, annonces TalkBack, contraste et réduction de la transparence
- Chargement optimisé des pochettes et images
- États hors connexion et messages d'erreur simplifiés
- Copie d'un diagnostic anonyme depuis les réglages
- Nettoyage des morceaux dont les fichiers ne sont plus accessibles
- Vérification et installation des mises à jour disponibles

# Technologies

- Kotlin
- Jetpack Compose
- Material Design 3
- Coroutines/Flow
- Room
- Media3
- Coil
- Retrofit
- Moshi
- Jetpack Glance
- Firebase

# Plateformes

- Android

# Captures d'écran

- Aucune ressource publiée dans le dépôt

# Vidéos

- Aucune ressource publiée dans le dépôt

# Téléchargements

## Android

- URL: "https://github.com/kidasniger/MusicPro/releases/download/v1.0-b240/MusicPro-v1.0-b240.apk"
- Version: "1.0-b240"
- Architecture: "APK universel"
- Taille: "21.9 MB"

## Windows

- Disponibilité : Non prise en charge (application Android)

## Linux

- Disponibilité : Non prise en charge (application Android)

## macOS

- Disponibilité : Non prise en charge (application Android)

# Liens

- GitHub: "https://github.com/kidasniger/MusicPro"
- Démo: "Non disponible"
- Site web: "https://musicpro.vercel.app/"
- Documentation: "Non disponible"
- Téléchargement: "https://github.com/kidasniger/MusicPro/releases/download/v1.0-b240/MusicPro-v1.0-b240.apk"
- YouTube: "Non disponible"
- Vidéo de présentation: "Non disponible"

# Nouveautés

- Navigation par onglets persistants et retour en haut au second appui
- Transitions partagées entre les pochettes, listes, albums et lecteur
- Retour prédictif Android 14 et feuilles du bas à deux ancrages
- Micro-interactions, égaliseur animé et changement de thème animé
- Accueil organisé en sections, recherche améliorée et filtres mémorisés
- En-têtes d'album et d'artiste enrichis, gestion des playlists améliorée
- Génération de playlists assistée par IA à partir de la bibliothèque locale
- Partage de cartes de morceaux
- Cibles tactiles de 48 dp et annonces TalkBack améliorées
- Contraste WCAG vérifié sur les thèmes et transparence ajustable
- Mise en page adaptative pour tablettes et orientation paysage
- Optimisation du chargement des images et des pochettes
- Gestion hors connexion améliorée et diagnostic anonyme
- 180 captures visuelles automatisées sur 10 écrans, 6 thèmes et 3 tailles
- Suppression des emojis de l'interface

# Changelog

## État du dépôt — 5 novembre 2026

La dernière release Android confirmée est `v1.0-b240`. Le lien de téléchargement et la version de référence de cette fiche sont alignés sur cette release. Les détails précis du changelog de `v1.0-b240` et la taille de son APK restent à confirmer.

Les derniers commits consultés documentent les lots de mises à jour 041 à 090. Le lot 081–090 apporte notamment :

- Vérification automatique des cibles tactiles à 48 dp dans l'arbre de sémantique
- Annonces TalkBack regroupées pour les lignes de morceaux
- Vérification du contraste WCAG : 4,5:1 pour le texte courant et 3:1 pour les grands textes
- Ajustement automatique de l'opacité du fond en verre lorsque le contraste est insuffisant
- Réglages « Texte plus lisible » et « Réduire la transparence », avec prise en compte du réglage système
- Adaptation WindowSizeClass : rail latéral sur tablette, détails album/artiste en deux volets et lecteur en deux colonnes en paysage
- Optimisation du flou et du chargement Coil (cache mémoire/disque, tailles de miniatures et préchargement)
- Bandeau hors connexion avec action Réessayer et messages simplifiés pour l'assistant IA et les paroles en ligne
- Action « Copier le diagnostic » anonyme dans Réglages › À propos
- 180 captures Roborazzi (10 écrans × 6 thèmes × 3 tailles), avec seuil de différence de 0,5 %

Validation rapportée dans le commit du lot 081–090 : 53 classes / 393 tests verts, lint sans erreur et chaînes FR/EN/AR/HA/DJE alignées.

## 1.0-b240

- Release Android la plus récente confirmée au 5 novembre 2026.
- APK de production référencé : `MusicPro-v1.0-b240.apk`.
- Détails fonctionnels et empreinte SHA-256 de cette release non renseignés faute de vérification de l’asset.

## 1.0-b207

- Ajout d'une barre de progression fine et tactile sur l'écran Lecture
- Ajout d'une barre de progression fine et tactile sur l'écran Paroles
- Ajout de la navigation par appui et glissement dans la progression
- Synchronisation de la progression avec les paroles
- Amélioration de la lisibilité des paroles
- Nettoyage des morceaux audio devenus inaccessibles
- Simplification des libellés visibles dans les écrans de paroles
- Suppression des emojis de l'interface
- Publication de l'APK de production

# SEO

## Title

MusicPro - Lecteur musical Android

## Description

MusicPro est un lecteur musical Android moderne pour organiser une bibliothèque audio, rechercher des morceaux, créer des playlists, afficher des paroles synchronisées et contrôler précisément la lecture, avec une interface adaptative et des options d'accessibilité.

## Keywords

- MusicPro
- lecteur musical Android
- application musique Android
- lecteur audio
- bibliothèque musicale
- gestion de musique
- playlists musicales
- paroles synchronisées
- karaoké
- égaliseur
- lecture en arrière-plan
- widget musique
- accessibilité Android
- lecteur musical hors ligne

## Open Graph Image

Non publiée. Aucune image Open Graph dédiée n’est actuellement référencée.

## Canonical URL

"https://github.com/kidasniger/MusicPro"

# Informations supplémentaires

Identifiant d'application Android : com.aistudio.musicpro.qkzmvp

Dernière release Android vérifiée dans ce fichier : v1.0-b240

APK de production référencé : MusicPro-v1.0-b240.apk

SHA-256 de l’APK v1.0-b240 : non publié / non vérifié.

SHA-256 historique de l’APK v1.0-b207 :

cf9aebf05700b8f9eacfaa0ba1a54f48c746b19cf3319a1eb57cb21ac68b2968

Note : l’empreinte SHA-256 et la taille de l’APK b240 ne sont pas renseignées, car l’asset officiel n’a pas été récupéré et vérifié lors de cette mise à jour.
