import os
try:
    from gtts import gTTS
except ImportError:
    print("Veuillez installer gTTS avec la commande: pip install gTTS")
    exit(1)

# Session 2: Problème avec Docker
session_2 = """
Bonjour Yosra. Bienvenue dans notre deuxième session de mentoring.
Aujourd'hui, j'ai vu que tu avais des problèmes avec le déploiement de ton application sur le serveur.
Le problème principal est que ta base de données et ton backend Spring Boot ne communiquent pas bien parce qu'ils ne sont pas sur le même réseau.
La solution que je te recommande est d'utiliser Docker Compose. 
Ça va te permettre de conteneuriser ton frontend Angular, ton backend Spring Boot et ta base PostgreSQL dans un seul environnement isolé.
Voici tes actions à faire pour la prochaine fois : premièrement, rédiger un Dockerfile pour le backend et le frontend. 
Deuxièmement, créer un fichier docker-compose.yml à la racine de ton projet pour orchestrer tout ça.
Les technologies abordées aujourd'hui sont Docker et Docker Compose. Bon courage !
"""

# Session 3: Problème de Performance JPA
session_3 = """
Salut Yosra ! Content de te revoir pour cette troisième session.
J'ai analysé les logs de ton backend, et j'ai trouvé pourquoi ta page de liste des fichiers est si lente.
Le problème initial est ce qu'on appelle le problème N plus 1 requêtes dans Hibernate. À chaque fois que tu récupères un fichier, Spring fait une requête supplémentaire pour récupérer le propriétaire.
La solution apportée est très simple : tu dois utiliser un EntityGraph ou un JOIN FETCH dans ta requête JPQL personnalisée au niveau de ton Repository.
Donc, les action items pour cette semaine sont : modifier l'interface FileRepository pour ajouter l'annotation EntityGraph, et supprimer le chargement Eager dans tes entités.
Les technologies principales de cette session sont Spring Data JPA et Hibernate. On en reparle demain.
"""

# Session 4: Authentification JWT
session_4 = """
Bonjour Yosra ! C'est notre quatrième et dernière session de la semaine.
On va parler de la sécurité de ton application. Le problème actuel est que n'importe qui peut appeler tes API sans être connecté.
La solution est de mettre en place une authentification basée sur des tokens JWT.
Pour cela, tu vas devoir intercepter chaque requête HTTP. 
Tes actions à faire sont : créer une classe utilitaire JwtUtils pour générer les tokens, et implémenter un filtre OncePerRequestFilter pour valider ces tokens à chaque appel.
Les technologies abordées aujourd'hui sont Spring Security et JWT. Tu as fait du super boulot jusqu'à présent !
"""

sessions = [
    ("session_2_docker.mp3", session_2),
    ("session_3_jpa.mp3", session_3),
    ("session_4_jwt.mp3", session_4)
]

print("Génération des fichiers audios en cours... (ça va prendre quelques secondes)")

for filename, text in sessions:
    print(f"Génération de {filename}...")
    tts = gTTS(text=text, lang='fr', slow=False)
    tts.save(filename)
    print(f"✅ {filename} créé !")

print("\nC'est terminé ! Tu peux maintenant uploader ces 3 fichiers dans ton application Angular.")
