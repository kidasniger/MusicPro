# MusicPro — historique des versions

## Prochaine mise à jour

- Réglages réorganisés en groupes à fond arrondi (24 dp) avec en-têtes en majuscules : Apparence, Lecture, Paroles, Intelligence artificielle, Stockage, Notifications, À propos ; chaque ligne affiche une icône en pastille 36 dp, la valeur choisie et un chevron.
- Champ de recherche dans les réglages : filtre en direct par libellé et synonymes (« couleur », « flou », « cache », « thème »), met en évidence le terme trouvé et ouvre le groupe concerné.
- Carte « Apparence » en tête de page : trois miniatures (Clair, Sombre, Auto) puis Minimal, Néon, Verre ; choix en un tap avec retour haptique léger.
- Nouveaux réglages « Taille du texte » (Petit, Normal, Grand, Très grand) et « Densité » (Confortable, Compacte : lignes de 52 dp au lieu de 64 dp), appliqués en direct.
- Interrupteur « Réduire les animations » : fondus de 120 ms, sans parallaxe ni flou animé ni pulsation du vinyle ; respecte aussi l'échelle d'animation système à 0.
- Réglage « Langue » : Français, English, العربية, Hausa, Zarma, Auto, via les langues d'application d'Android ; chaînes en dur déplacées vers les ressources.
- Nouvelle ligne « Son » dans Lecture : égaliseur en feuille du bas avec préréglages (Normal, Basses, Voix, Classique, Pop, Rock), réglage par bande −/+ et bouton « Comparer » qui alterne avec/sans effet pendant 3 secondes.
- « Exporter mes réglages » et « Importer » : fichier JSON (thème, style, verre, karaoké, favoris) via le sélecteur système ; la version du schéma est validée et un résumé est affiché avant application.
- Section IA : clé Groq masquée par défaut, « Tester la clé » avec résultat clair et bouton « Supprimer » ; la clé reste chiffrée et n'est jamais journalisée.
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
