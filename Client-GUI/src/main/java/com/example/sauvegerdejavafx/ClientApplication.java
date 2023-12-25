package com.example.sauvegerdejavafx;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.*;
import java.net.Socket;
import java.nio.file.*;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Optional;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class ClientApplication extends Application {
    HashSet<String> extensionsSet = null;

    // Clé XOR pour le chiffrement et le déchiffrement
    private static final byte XOR_KEY = 0x0F;
    private TextField serverIPTextField;
    private Button connexionButton;
    private TextArea resultTextArea;  // Zone de texte pour afficher les résultats
    private Socket socket;
    private DataOutputStream dos;

    private BufferedReader br;
    private BufferedReader kb;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Client GUI");

        serverIPTextField = new TextField();
        serverIPTextField.setPromptText("Entrez l'adresse IP du serveur");

        connexionButton = new Button("Connexion au serveur");
        connexionButton.setOnAction(e -> connectToServer());

        resultTextArea = new TextArea();  // Zone de texte pour afficher les résultats
        resultTextArea.setEditable(false);  // Empêche l'édition du texte

        VBox layout = new VBox(10);
        layout.getChildren().addAll(new Label("Adresse IP du serveur:"), serverIPTextField, connexionButton, resultTextArea);
        Scene scene = new Scene(layout, 400, 300);

        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void connectToServer() {
        String serverIP = serverIPTextField.getText();

        try {
            socket = new Socket(serverIP, 8080);
            dos = new DataOutputStream(socket.getOutputStream());
            br = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            // Affiche le message de connexion réussie
            resultTextArea.appendText("Connexion réussie au serveur.\n");

            // Demande à l'utilisateur de choisir 1 ou 2
            resultTextArea.appendText("1. Envoyer de nouvelles extensions\n");
            resultTextArea.appendText("2. Télécharger une sauvegarde\n");

            // Attend que l'utilisateur entre 1 ou 2
            handleUserChoice();
        } catch (Exception e) {
            e.printStackTrace();
            resultTextArea.appendText("Erreur de connexion au serveur.\n");
        }
    }

    private void handleUserChoice() {
        TextInputDialog choiceDialog = new TextInputDialog();
        choiceDialog.setHeaderText(null);
        choiceDialog.setContentText(" '1' Envoyer de nouvelles extensions ou '2' Télécharger une sauvegarde ");
        Optional<String> userInput = choiceDialog.showAndWait();

        userInput.ifPresent(choice -> {
            try {
                if ("1".equals(choice)) {
                    dos.writeBytes(1 + "\n");
                    handleOption1();
                } else if ("2".equals(choice)) {
                    dos.writeBytes(2 + "\n");
                    handleOption2();
                } else {
                    resultTextArea.appendText("Choix invalide.\n");
                    showAlert("Choix invalide, veuillez relancer le programme");
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    private void handleOption1() throws IOException {
        // Demande à l'utilisateur d'entrer les extensions
        TextInputDialog extensionDialog = new TextInputDialog();
        extensionDialog.setHeaderText(null);

        // AJOUTS EXTENSIONS A SAUVER DOFFICE :
        extensionsSet = new HashSet<>();

        // Ajouter les 10 extensions de fichiers au HashSet
        extensionsSet.add(".txt");
        extensionsSet.add(".pdf");
        extensionsSet.add(".jpg");
        extensionsSet.add(".jpeg");
        extensionsSet.add(".png");
        extensionsSet.add(".docx");
        extensionsSet.add(".xlsx");
        extensionsSet.add(".mp3");
        extensionsSet.add(".mp4");
        extensionsSet.add(".html");

        // la sauvegarde va automatqiuement sauver les fichiers avec les extensions comprises dans le extensionsSet
        resultTextArea.appendText("la sauvegarde va automatqiuement sauver les fichiers avec les extensions suivantes : " + "\n");

        System.out.println("----Début----");
        for (String s : extensionsSet) {
            resultTextArea.appendText(s.toString() + "\n");
        }

        extensionDialog.setContentText("Extensions reçues du serveur :\n");
        String str;

        while( !(str = br.readLine()).equals("fin") ){
            //resultTextArea.appendText(str + "\n");
            //System.out.println(str);
        }
        System.out.println("----"+str+"----"); // ou mettre System.out.println("----fin----");


        TextInputDialog newExtensionsDialog = new TextInputDialog();
        newExtensionsDialog.setHeaderText(null);
        newExtensionsDialog.setContentText("Entrez les extensions à ajoutés en plus au format (txt,jpeg,jpg,...):");
        String newExtensions = newExtensionsDialog.showAndWait().orElse("");

        // Vérifie si la chaîne est vide ou uniquement constituée d'espaces
        while (newExtensions.trim().isEmpty()) {
            // Demande au client de saisir à nouveau les données
            newExtensionsDialog.setContentText("La saisie est vide. Entrez les nouvelles extensions au format (txt,jpeg,jpg,...):");
            newExtensions = newExtensionsDialog.showAndWait().orElse("");
        }
        // À ce stade, newExtensions contient une valeur non vide que vous pouvez envoyer au serveur
        dos.writeBytes(newExtensions + "\n");
        String[] etensionsDeUserArray = newExtensions.split(",");

        // Inscrire les nouvelles extensions dans extensions.txt
        Arrays.stream(etensionsDeUserArray).map(s -> "." + s).forEach(s -> extensionsSet.add(s));
        extensionsSet.forEach(System.out::println);


        // Affiche un message et demande le chemin du dossier
        resultTextArea.appendText("Extensions mises à jour avec succès.\n");
        resultTextArea.appendText("Entrez le chemin du dossier à sauvegarder :\n");


        TextInputDialog folderDialog = new TextInputDialog();
        folderDialog.setHeaderText(null);
        folderDialog.setContentText("Entrez le chemin du dossier à sauvegarder:");
        String sourceDir = folderDialog.showAndWait().orElse("");

        while (sourceDir.trim().isEmpty()) {
            // Demande au client de saisir à nouveau les données
            folderDialog.setContentText("La saisie est vide. Entrez le chemin du dossier à sauvegarder:");
            sourceDir = folderDialog.showAndWait().orElse("");
        }

        // Attend que le serveur soit prêt pour la sauvegarde
        sendBackup(sourceDir);

        showAlert("La sauvegarde a été effectuée avec succès.\n");
    }

    private void handleOption2() throws IOException {
        InputStream inputStream = socket.getInputStream();
        ZipInputStream zipInputStream = new ZipInputStream(inputStream);
        ZipEntry zipEntry;
        String str;
        // Affiche les sauvegardes disponibles
        TextInputDialog backupListDialog = new TextInputDialog();
        backupListDialog.setHeaderText(null);
        backupListDialog.setContentText("Choisissez:\n");
        // le server envoi toutes les backup + le mot Choisissez à la fin donc tant que le server envoi pas Choisissez
        // alors j'affiche les backup reçu au client
        while(!((str = br.readLine()).equals("Choisissez :"))){
            // receive from the server
            resultTextArea.appendText(str + "\n");
        }
        String selectedBackup = backupListDialog.showAndWait().orElse("");
        while (selectedBackup.trim().isEmpty()) {
            // Demande au client de saisir à nouveau les données
            backupListDialog.setContentText("La saisie est vide. Choisissez:");
            selectedBackup = backupListDialog.showAndWait().orElse("");
        }

        dos.writeBytes(selectedBackup + "\n");

        //je gere l'erreur que si le client rentre un dossier inexistant à récupérer
        if((str = br.readLine()).equals("le dossier que vous avez choissis n'existe pas")){
            System.out.println(str);
            showAlert("dossier inexistant importé , veuillez relancer le programme");
        }
        resultTextArea.appendText("vous importez la sauvegarde : " + str);

        TextInputDialog selectedBackupDialog = new TextInputDialog();
        selectedBackupDialog.setHeaderText(null);
        selectedBackupDialog.setContentText("Ou voulez vous mettre la sauvegarde ? (indiquer le chemin complet) avec " +
                "en final le nom du nouveau dossier :");
        String pathFolders = selectedBackupDialog.showAndWait().orElse("");
        while (pathFolders.trim().isEmpty()) {
            // Demande au client de saisir à nouveau les données
            selectedBackupDialog.setContentText("La saisie est vide. Ou voulez vous mettre la sauvegarde ? (indiquer le chemin complet) avec \" +\n" +
                    "\"en final le nom du nouveau dossier :");
            pathFolders = selectedBackupDialog.showAndWait().orElse("");
        }



        while ((zipEntry = zipInputStream.getNextEntry()) != null) {
            String entryName = zipEntry.getName();

            if (entryName != null && !entryName.isEmpty()) {
                Path filePath = Paths.get(pathFolders, entryName);

                // Si l'entrée est un répertoire, assurez-vous de le créer
                if (zipEntry.isDirectory()) {
                    Files.createDirectories(filePath);
                } else {
                    // Si c'est un fichier, créez le fichier et copiez les données
                    Files.createDirectories(filePath.getParent());
                    try (OutputStream outputStream = Files.newOutputStream(filePath)) {
                        decryptAndCopyFile(zipInputStream, outputStream);
                    }
                }
            }
            zipInputStream.closeEntry();
        }
        System.out.printf("Dossier bien importé");
        showAlert("La sauvegarde a été téléchargée avec succès.");
    }

    private void sendBackup(String sourceDir) throws IOException {
        resultTextArea.appendText("Demande de sauvegarde envoyée au serveur.\n");
        OutputStream outputStream = socket.getOutputStream();
        ZipOutputStream zipOutputStream = new ZipOutputStream(outputStream);

        Files.walk(Paths.get(sourceDir))
                .forEach(filePath -> {
                    try {
                        Path relativePath = Paths.get(sourceDir).relativize(filePath);

                        if (Files.isDirectory(filePath)) {
                            zipOutputStream.putNextEntry(new ZipEntry(relativePath.toString() + "/"));
                            zipOutputStream.closeEntry();
                        }
                        else {
                            // Vérification de l'extension avant d'ajouter au Zip
                            String fileExtension = getFileExtension(filePath);
                            System.out.println("fileExtension : " + fileExtension );
                            if (extensionsSet.contains(fileExtension)) {
                                zipOutputStream.putNextEntry(new ZipEntry(relativePath.toString()));
                                encryptAndCopyFile(filePath, zipOutputStream);
                                zipOutputStream.closeEntry();
                            } else {
                                System.out.println("L'extension non autorisée du fichier " + filePath + " a été ignorée.");
                            }
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });
        zipOutputStream.close();
    }

    private static String getFileExtension(Path filePath) {
        String fileName = filePath.getFileName().toString();
        int dotIndex = fileName.lastIndexOf('.');
        return (dotIndex == -1) ? "" : fileName.substring(dotIndex);
    }
    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Information");
        alert.setHeaderText(null);
        alert.setContentText(message);

        // Ajoute un événement pour fermer l'application lorsque l'utilisateur clique sur OK
        alert.setOnCloseRequest(event -> {
            Platform.exit();
            System.exit(0); // Assurez-vous de fermer l'application complètement
        });

        alert.showAndWait();
    }

    private static void encryptAndCopyFile(Path sourcePath, ZipOutputStream zipOutputStream) throws IOException {
        try (InputStream inputStream = Files.newInputStream(sourcePath)) {
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                // XOR encryption
                for (int i = 0; i < bytesRead; i++) {
                    buffer[i] = (byte) (buffer[i] ^ XOR_KEY);
                }
                zipOutputStream.write(buffer, 0, bytesRead);
            }
        }
    }

    private static void decryptAndCopyFile(ZipInputStream zipInputStream, OutputStream outputStream) throws IOException {
        byte[] buffer = new byte[1024];
        int bytesRead;
        while ((bytesRead = zipInputStream.read(buffer)) != -1) {
            // XOR decryption
            for (int i = 0; i < bytesRead; i++) {
                buffer[i] = (byte) (buffer[i] ^ XOR_KEY);
            }
            outputStream.write(buffer, 0, bytesRead);
        }
    }
}
