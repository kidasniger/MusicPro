---
name: "MusicPro"
slug: "musicpro"
version: "1.0-b207"
status: "Stable"
category: "Lecteur musical"
author: "kidasniger"
license: "À renseigner"
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
demo: "https://github.com/kidasniger/MusicPro"
website: "https://github.com/kidasniger/MusicPro"
documentation: "https://github.com/kidasniger/MusicPro"
download: "https://github.com/kidasniger/MusicPro/releases/download/v1.0-b207/MusicPro-v1.0-b207.apk"
icon: "https://raw.githubusercontent.com/kidasniger/MusicPro/main/app/src/main/res/drawable/musicpro_logo_square.png"
cover: "À renseigner"
---

# Description

MusicPro est une application Android de lecture musicale conçue pour gérer et écouter une bibliothèque audio locale avec une interface sombre moderne. L'application propose la lecture en arrière-plan, les playlists, les favoris, la recherche, les paroles synchronisées, un mode karaoké, un égaliseur, un widget d'accueil et un système de mise à jour.

# Description courte

Lecteur musical Android moderne avec bibliothèque locale, lecture en arrière-plan, playlists, paroles synchronisées, recherche, karaoké et contrôles de lecture avancés.

# Fonctionnalités

- Détection et organisation des morceaux présents sur l'appareil
- Recherche par titre, artiste, album et playlist
- Historique des morceaux récemment écoutés
- Lecture avec précédent, suivant, répétition et lecture aléatoire
- Progression de lecture fine et tactile sur l'écran Lecture
- Progression de lecture fine et tactile sur l'écran Paroles
- Appui ou glissement sur la progression pour avancer ou revenir à n'importe quel instant
- Boutons de déplacement rapide de 10 secondes en avant ou en arrière
- Synchronisation de la position audio avec les paroles
- Affichage des paroles synchronisées avec mise en évidence de la ligne active
- Recherche de paroles en ligne
- Génération assistée de paroles synchronisées
- Import et enregistrement des paroles associées à un morceau
- Mode karaoké avec réglage du décalage et de l'affichage
- Favoris
- Playlists personnalisées avec réorganisation des morceaux
- File de lecture
- Égaliseur avec réglage des bandes audio
- Lecture sans interruption entre les morceaux
- Reprise de la lecture
- Lecture automatique de la file
- Notification multimédia et commandes de lecture en arrière-plan
- Widget de contrôle depuis l'écran d'accueil
- Mise à jour de l'application depuis les releases officielles
- Nettoyage automatique des morceaux devenus inaccessibles
- Suppression des références obsolètes afin d'éviter l'affichage de morceaux disparus
- Interface sans emojis ni symboles décoratifs utilisés comme texte
- Libellés utilisateur simplifiés, sans exposition des détails techniques des services utilisés

# Expérience utilisateur

MusicPro privilégie une interface centrée sur la musique et les actions utiles.

Les écrans principaux évitent les répétitions inutiles et n'affichent pas en permanence des informations techniques sur le fonctionnement interne de l'application. Les fonctions sont présentées avec des libellés simples, des icônes et des contrôles tactiles cohérents.

La recherche est présentée comme une action directe de recherche dans la bibliothèque, sans multiplier les rappels sur le fonctionnement hors connexion.

La progression de lecture utilise une barre visuelle fine et discrète. Elle reste entièrement interactive : l'utilisateur peut toucher la barre ou faire glisser la position pour changer l'instant de lecture.

# Paroles

L'écran Paroles utilise la même progression de lecture que l'écran Lecture afin de conserver une position audio unique.

Lorsqu'une nouvelle position est sélectionnée :

- la lecture audio est déplacée immédiatement après validation ;
- l'affichage du temps est mis à jour ;
- les paroles se repositionnent sur la ligne correspondant au nouvel instant ;
- la ligne active reste clairement identifiable ;
- les lignes non actives restent suffisamment contrastées pour rester lisibles.

# Nettoyage de la bibliothèque

L'application vérifie les références locales conservées dans sa bibliothèque afin d'écarter les morceaux dont la source audio n'est plus accessible.

Le nettoyage est effectué lors de l'actualisation de la bibliothèque et peut également être déclenché lorsqu'une erreur de lecture révèle qu'un fichier a disparu ou n'est plus accessible.

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

# Téléchargements

## Android

- URL: "https://github.com/kidasniger/MusicPro/releases/download/v1.0-b207/MusicPro-v1.0-b207.apk"
- Version: "1.0-b207"
- Architecture: "À renseigner"
- Taille: "21.9 MB"
- SHA-256: "cf9aebf05700b8f9eacfaa0ba1a54f48c746b19cf3319a1eb57cb21ac68b2968"

# Liens

- GitHub: "https://github.com/kidasniger/MusicPro"
- Site web / page d'accueil: "https://github.com/kidasniger/MusicPro"
- Documentation: "https://github.com/kidasniger/MusicPro"
- Téléchargement: "https://github.com/kidasniger/MusicPro/releases/download/v1.0-b207/MusicPro-v1.0-b207.apk"
- Release: "https://github.com/kidasniger/MusicPro/releases/tag/v1.0-b207"
- YouTube: "À renseigner"
- Vidéo de présentation: "À renseigner"

# Nouveautés

- Remplacement de la progression standard par une barre fine, discrète et tactile sur les écrans Lecture et Paroles.
- Appui et glissement sur la progression pour se déplacer précisément dans le morceau.
- Synchronisation de la nouvelle position de lecture avec les paroles.
- Amélioration de la lisibilité des paroles non actives.
- Suppression des noms de services et détails techniques visibles dans l'expérience de recherche et de génération des paroles.
- Nettoyage automatique des références vers les morceaux dont les fichiers ont disparu.
- Suppression des emojis et symboles décoratifs affichés comme texte dans l'interface.
- Simplification de plusieurs écrans pour réduire les informations répétitives et garder l'accent sur la musique.

# Changelog

## 1.0-b207

- Barre de progression fine et tactile sur Lecture.
- Barre de progression fine et tactile sur Paroles.
- Avance et retour par appui ou glissement.
- Synchronisation de la progression avec les paroles.
- Nettoyage des morceaux audio devenus inaccessibles.
- Nettoyage des libellés techniques visibles dans les écrans de paroles.
- Suppression des emojis de l'interface.
- Validation du build de production et publication de l'APK.

# SEO

## Title

MusicPro - Lecteur musical Android

## Description

MusicPro est un lecteur musical Android moderne pour gérer une bibliothèque audio locale, rechercher et écouter des morceaux, utiliser des playlists, afficher des paroles synchronisées et contrôler facilement la lecture.

## Keywords

- MusicPro
- lecteur musical Android
- musique Android
- bibliothèque musicale
- lecteur audio
- playlists
- paroles synchronisées
- karaoké
- égaliseur
- widget musique
- lecture audio

## Open Graph Image

À renseigner

## Canonical URL

https://github.com/kidasniger/MusicPro

# Informations supplémentaires

Identifiant d'application Android : com.aistudio.musicpro.qkzmvp

Dernière release Android vérifiée : v1.0-b207

APK de production : MusicPro-v1.0-b207.apk

SHA-256 de l'APK v1.0-b207 :
cf9aebf05700b8f9eacfaa0ba1a54f48c746b19cf3319a1eb57cb21ac68b2968
