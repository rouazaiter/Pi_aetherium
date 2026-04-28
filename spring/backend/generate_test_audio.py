import os
try:
    from gtts import gTTS
except ImportError:
    print("Veuillez installer gTTS avec la commande: pip install gTTS")
    exit(1)

# Le texte de notre fausse session de consulting/mentorat
conversation = """
Bonjour Yosra. Merci d'avoir pris cet appel de consulting. 
Alors, j'ai bien regardé ton problème de téléchargement de fichiers dans Angular et Spring Boot.
Le problème principal que tu rencontres est une erreur CORS avec un statut zéro dans ton frontend quand tu essaies de télécharger de grosses vidéos.
La solution que je te propose est de ne pas utiliser le HttpClient d'Angular pour les téléchargements, car les requêtes Blob sont très sensibles aux règles de sécurité du navigateur.
À la place, tu devrais créer une balise a invisible en HTML, y mettre l'URL de ton endpoint Spring Boot, et forcer le clic.
Du côté backend, n'oublie pas d'exposer les headers Content-Disposition et Content-Type dans ton SecurityConfig.
Pour la prochaine session, j'aimerais que tu mettes ça en place. 
Donc tes actions à faire sont : premièrement, remplacer le code HttpClient par un simple lien HTML. Deuxièmement, mettre à jour la configuration CORS dans Spring Security.
On utilisera toujours Angular et Spring Boot. On se revoit la semaine prochaine !
"""

print("Génération de l'audio en cours... (ça peut prendre quelques secondes)")

tts = gTTS(text=conversation, lang='fr', slow=False)
filename = "session_consulting_test.mp3"
tts.save(filename)

print(f"✅ Fichier '{filename}' généré avec succès !")
print("Tu peux maintenant l'uploader dans ton application Angular pour tester le Bilan de Session.")
