# Application de Sauvegarde de Fichiers 

## Introduction
Cette application a été développée pour simplifier le processus de sauvegarde de fichiers en permettant à l'utilisateur de spécifier les extensions de fichiers à sauvegarder en 
plus de celles de bases qui sont : "txt, pdf, jpg, jpeg, png, docx, xlsx, mp3, mp4, html". 
Les fichiers sont chiffrés coté client avec la méthode XOR avant d'être envoyé au serveur. 
Le client déchiffre également les données lorsqu'il les récupère depuis le serveur.

Cette partie est la partie client de l'application, pour la partie Server veuillez suivre ce lien [Server](https://github.com/damien-mathieu1/TD-Cloud-Sauvegarde)

## Prérequis
Avant d'utiliser l'application, assurez-vous d'avoir Java installé sur votre machine. Vous pouvez télécharger la dernière version de Java sur le site officiel d'Oracle : [Télécharger Java](https://www.oracle.com/java/technologies/javase-downloads.html)

## Configuration
Le client à été créer avec javaFX. 
Pour executer le client il faut ouvrir le client dans intellij idea. 

## Utilisation
1. **Exécution de l'Application**
   - Lancer l'application depuis intellij idea
   - Pour exécuter le server veuillez suivre ce lien [Server](https://github.com/damien-mathieu1/TD-Cloud-Sauvegarde)

2. **Sauvegarde des Fichiers**
   - L'application explorera récursivement le répertoire spécifié et sauvegardera les fichiers correspondant aux extensions spécifiées dans `extensions.txt`.
   - Les fichiers sauvegardés seront stockés dans un répertoire nommé `Backup_date` créé dans le répertoire de l'application.

## Avertissement
L'application ne modifie pas les fichiers d'origine. Elle crée une copie des fichiers chiffrés correspondant aux extensions spécifiées dans le répertoire `Backup_date`.

Merci d'utiliser notre application de sauvegarde de fichiers  !
