Pour ce projet, nous avons choisi de coder en java, un langage de programmation orientée objet.
Ainsi, notre code est organisé de la façon suivante.

Structure de notre code :

Nous avons au total huit classes :
- une classe de lecture de la trace : Trace
- quatre classes de protocole de trame Ethernet : Trame, IPv4, TCP, HTTP
- une classe d'affichage : Affichage
- une classe pour notre interface graphique : InterfaceGraphique
- une classe exécutant notre interface graphique : InterfaceGraphiqueMain

À présent, voyons la structure globale de chaque classe :

Au sein de la classe Trace :
- deux attributs statiques, un permettant de sauvegarder la liste des classes, un autre contenant la liste des protocoles rencontrés dans la trace
- deux getters sur les attributs, afin qu'on puisse récupérer la liste des trames et des protocoles dans chacune des classes
- un constructeur qui lit chaque ligne de notre fichier trace et initialise notre liste de trames une fois qu'on rencontre un offset égale à 0000

Au sein de chaque classe de protocole :
- des attributs représentant les entêtes du protocole, y compris les options que l'on a jugées intéressantes
- un constructeur initialisant ces attributs à des valeurs arbitraires
- une méthode lire_"nom_du_protocole"() permettant d'affecter les valeurs lues aux attributs
- une méthode getCommentary() qui renvoie une chaîne de caractères contenant les éléments qu'on a jugés importants à préciser en commentaire lors de l'affichage de notre graphique (ports destination et source, options...)
- une méthode nomProtocole qui renvoie le nom du protocole le plus encapsulé dans la trame
- une méthode get"nom_du_protocole"() qui renvoie le protocole qu'encapsule le protocole courant
- des getters sur quelques entêtes

Notons qu'en plus de ces éléments, on retrouve une méthode de conversion de chaînes de caractères hexadécimaux en chaînes de caractères ASCII ainsi qu'une méthode permettant de retrouver le premier fragment lorsque la trame est fragmentée dans la classe HTTP, et quelques méthodes permettant de vérifier si les trames ont un soucis (éléments non capturés par exemple) dans la classe TCP.

Au sein de notre classe d'affichage :
- une seule méthode permettant d'obtenir l'affichage souhaitée pour une seule trame dans notre visualisateur de flux

Au sein de notre classe d'interface graphique :
 - un seul constructeur dans lequel on initialise deux panneaux, un contenant l'affichage et un autre contenant les boutons de filtrage, d'ouverture et de sauvegarde de fichiers, d'ouverture et de fermeture de fenêtre et d'effaçage
 
Au sein de notre classe d'exécution :
- une méthode main permettant de lancer notre visualisateur de flux