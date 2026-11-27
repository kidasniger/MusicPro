# MusicPro — historique des versions

## Prochaine mise à jour

- **102 — Refonte minimaliste appliquée à toute l'application** : le style Minimal devient le style de référence (papier chaud la nuit, accent terracotta, contours fins, titres en Source Serif 4). Les installations existantes basculent une seule fois vers Minimal au premier lancement de la mise à jour ; Néon et Verre restent disponibles dans Réglages › Apparence › Style.
- **102 — Un accent, pas deux** : les ~520 couleurs violet/cyan néon codées en dur dans les écrans (bibliothèque, recherche, paroles, réglages, dialogues…) sont remplacées par les jetons du thème — chaque écran suit désormais le style actif, l'accent est unique et cohérent.
- **102 — Composant pochette unique sur tous les écrans** : `MusicProArtwork` centralise la chaîne d'affichage des pochettes (image, repli icône garanti) et est déployé sur les 12 écrans concernés — bibliothèque, accueil, nouveau, album, artiste, playlist et détail, favoris, file d'attente, recherche, actions du morceau, dialogues ; seul le héros artiste en nuances de gris conserve son chargement dédié.
- **102 — Palette sémantique conforme à la maquette** : succès #6FCF97 et favori #E0708C (cœur actif) remplacent les valeurs néon historiques ; conteneurs et halos alignés.
- **Pochettes auto-réparées** : le cache de l'app peut être purgé par le système (Samsung Device Care, stockage plein), ce qui faisait disparaître toutes les pochettes jusqu'à un rescan manuel. Au démarrage, les fichiers de jaquettes manquants sont recréés depuis l'image embarquée des morceaux — les pochettes reviennent sans aucune action, avec un message discret confirmant la restauration.
- **Écran Paroles plus robuste** : si l'image de fond ne charge plus, l'écran retombe sur son halo coloré au lieu d'un fond vide.

- **101 — Aperçu karaoké « lueur douce » (onglet Lecture)** : la ligne active s'affiche en cyan néon avec un halo doux et une légère montée d'échelle ; fenêtre de 5 lignes centrée sur la progression, voisins atténués, badge « Paroles synchronisées ».
- **101 — Transport équilibré** : Aléatoire et Répéter deviennent des boutons ronds de 44 dp en verre dépoli, état actif en cyan avec point lumineux, cibles tactiles conformes à la rangée.
- **101 — Vinyle vivant** : le disque entier tourne en continu pendant la lecture (18 s/tour), la pochette centrale reste stable et un reflet lumineux balaie la surface du disque.
- **101 — Titres propres** : les compteurs de doublons « (0) » / « [2] » sont masqués, les années « (2024) » et mentions « (Live) » restent visibles ; sans paroles, un mini-vinyle animé et un égaliseur à barres remplacent l'invitation statique.
- **101 — Code réorganisé** : NowPlayingScreen.kt scindé en fichiers thématiques (barre supérieure, pochette, contrôles, onglets) pour la maintenabilité.

- **041 — Pochette immersive pleine largeur** : la pochette s'affiche en haut sur toute la largeur, sans cadre, et se fond vers le fond avec un dégradé vertical de 160 dp. Le fond de l'écran reprend la couleur dominante de la pochette (assombrie de 35 %) et se met à jour en 400 ms à chaque morceau.
- **042 — Bandeau titre/artiste en pilule de verre** : titre (2 lignes max) et artiste dans une pilule de verre, avec à droite trois boutons ronds de 40 dp (Radio, File d'attente, J'aime). Le cœur s'anime (échelle 1→1,25→1, 220 ms) et vibre légèrement quand on aime.
- **043 — Barre de progression fine et tactile** : barre de 4 dp (10 dp au toucher) avec temps écoulé à gauche et restant/total à droite, zone tactile de 40 dp, aperçu flottant du temps au-dessus du doigt pendant le glissement ; toujours sans `Slider` Material.
- **044 — Boutons de transport en verre** : lecture/pause en cercle de 88 dp, précédent et suivant en 64 dp, aléatoire et répéter en icônes 28 dp aux extrémités ; l'état actif s'affiche par un point sous l'icône (accessibilité).
- **045 — Onglets inférieurs du lecteur** : pilule de verre à quatre onglets (Lecture, Paroles, Réglages audio, Infos) ; le changement glisse horizontalement avec fondu et l'onglet actif est dans une capsule plus claire. Paroles synchronisées défilantes et égaliseur complet intégrés au lecteur.
- **046 — Bouton « Passer à la vidéo »** : si un fichier vidéo existe à côté du morceau, une pilule apparaît au-dessus du titre ; la position de lecture est conservée à ±100 ms près dans les deux sens.
- **047 — Gestes du lecteur** : glisser vers le bas réduit en mini-lecteur (la pochette voyage avec la transition partagée), double tap à gauche/droite = −10 s / +10 s avec onde visuelle, appui long sur la pochette = menu (playlist, album, artiste, partager).
- **048 — Présentation du lecteur** : dans Réglages › Lecture, choisir Pochette (carrée, par défaut), Vinyle (rotation 33 tours, geste horizontal réservé au morceau précédent/suivant) ou Minimal (texte seul sur fond uni). La rotation respecte « Réduire les animations » et se met en pause avec la lecture.
- **049 — Contrôle du volume et sortie audio** : icône « appareils » en haut à droite ouvrant une feuille du bas listant haut-parleur, casque filaire et Bluetooth avec l'appareil actif coché, plus les boutons −/+ de volume ; un toast discret signale les branchements (« Casque connecté »).
- **050 — Minuterie de sommeil** : dans le menu ⋯ du lecteur, minuteries de 5 à 60 minutes ou « Fin du morceau » ; une pastille de compte à rebours apparaît près du titre et le volume baisse en fondu pendant les 10 dernières secondes avant l'arrêt.

- Réglages réorganisés en groupes à fond arrondi (24 dp) avec en-têtes en majuscules : Apparence, Lecture, Paroles, Intelligence artificielle, Stockage, Notifications, À propos ; chaque ligne affiche une icône en pastille 36 dp, la valeur choisie et un chevron.
- Champ de recherche dans les réglages : filtre en direct par libellé et synonymes (« couleur », « flou », « cache », « thème »), met en évidence le terme trouvé et ouvre le groupe concerné.
- Carte « Apparence » en tête de page : trois miniatures (Clair, Sombre, Auto) puis Minimal, Néon, Verre ; choix en un tap avec retour haptique léger.
- Nouveaux réglages « Taille du texte » (Petit, Normal, Grand, Très grand) et « Densité » (Confortable, Compacte : lignes de 52 dp au lieu de 64 dp), appliqués en direct.
- Interrupteur « Réduire les animations » : fondus de 120 ms, sans parallaxe ni flou animé ni pulsation du vinyle ; respecte aussi l'échelle d'animation système à 0.
- Réglage « Langue » : Français, English, العربية, Hausa, Zarma, Auto, via les langues d'application d'Android ; chaînes en dur déplacées vers les ressources.
- Nouvelle ligne « Son » dans Lecture : égaliseur en feuille du bas avec préréglages (Normal, Basses, Voix, Classique, Pop, Rock), réglage par bande −/+ et bouton « Comparer » qui alterne avec/sans effet pendant 3 secondes.
- « Exporter mes réglages » et « Importer » : fichier JSON (thème, style, verre, karaoké, favoris) via le sélecteur système ; la version du schéma est validée et un résumé est affiché avant application.
- Section IA : clé Groq masquée par défaut, « Tester la clé » avec résultat clair et bouton « Supprimer » ; la clé reste chiffrée et n'est jamais journalisée.
- Stockage de la clé IA auto-réparé : si le Keystore ou le fichier chiffré devient illisible (fréquent sur Samsung après un changement de code de verrouillage, une mise à jour système ou une restauration Smart Switch), les données corrompues sont supprimées puis le stockage se recrée au lieu de rester bloqué ; le fichier de la clé est aussi exclu des sauvegardes Google et des transferts d'appareil, et un message clair invite à effacer les données de l'app si le stockage sécurisé reste indisponible.
- Chaque groupe de réglages propose un menu ⋯ « Réinitialiser cette section » : une confirmation en feuille du bas précise exactement ce qui sera remis par défaut, avec « Annuler » au même niveau visuel que « Réinitialiser ».

- Interface plus cohérente en mode sombre, clair et automatique.
- Navigation et contrôles tactiles plus accessibles.
- Parcours de recherche, bibliothèque et lecture affinés.
- Écran de démarrage raccourci pour un accès plus rapide.
- Libellés utilisateur simplifiés, sans exposition de détails techniques.
- Nouveau réglage « Effet verre » à trois niveaux : « Le plus rapide » (fond translucide sans flou), « Équilibré » (léger flou et un reflet, par défaut) et « Qualité » (flou réel, reflets, bordure lumineuse, grain léger et ombre douce, Android 12 et plus).
- Nouveau réglage « Teinte du verre » : 9 pastilles (Aucune, Violet, Rouge, Orange, Jaune, Vert, Cyan, Bleu, Magenta) et une barre arc-en-ciel pour choisir une teinte libre, avec aperçu en direct.
- Nouveaux curseurs « Film » (opacité du voile de couleur du verre, 50 % par défaut) et « Onglet actif » (opacité de la capsule de l'onglet sélectionné, 26 % par défaut), avec aperçu en direct.
- Nouveau style « Minimal » (papier chaud, accent terracotta, contours fins) en clair et en sombre.
- « Apparence » (Clair, Sombre, Auto, Sombre le soir) et « Style » (Minimal, Néon, Verre) sont deux réglages indépendants, avec trois cartes d'aperçu et un fondu de 250 ms. L'ancien réglage de thème est migré automatiquement.
- Nouveau choix d'accent (Terracotta, Bleu, Vert, Violet, Selon la pochette) et plage horaire pour « Sombre le soir ».
- Barres d'état et de gestes transparentes, avec icônes adaptées au thème affiché.


- Le lecteur n'utilise plus les curseurs audio standards : progression tactile avec **−10 s / +10 s** et volume avec **− / +**.
- L'égaliseur utilise des commandes directes **− / +** pour chaque bande, avec **Activer / Désactiver** et **Tout rétablir**.
- Le bouton **Tout rétablir** du mode karaoké remet la taille du texte, la couleur active et le décalage à leurs valeurs par défaut.
- Le geste horizontal du vinyle reste réservé aux morceaux précédent/suivant.
