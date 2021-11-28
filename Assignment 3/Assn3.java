import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPClient;
import java.io.File;
import java.io.IOException;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import java.util.stream.Collectors;
import java.util.function.Function;

public class Assn3 {

    public static FTPClient ftp;

    private static class CommandNotFoundException extends Exception {
        public CommandNotFoundException(String message) {
            super(message);
        }
    }

    private static class InvalidCommandException extends Exception {
        public InvalidCommandException(String message) {
            super(message);
        }
    }

    public static void main(String[] args) throws CommandNotFoundException, IOException, InvalidCommandException {
        ftp = new FTPClient();
        ftp.connect(args[0]);
        String[] credentials = args[1].split(":");
        ftp.login(credentials[0], credentials[1]);
        for (int i = 2; i < args.length; i++) {
            processCommand(args[i]);
        }
    }

    public static void processCommand(String command) throws CommandNotFoundException, IOException, InvalidCommandException {
        // With "[^"]*", we match complete "double-quoted strings"
        // | - OR
        // With '[^']*', we match complete 'single-quoted strings'
        // | - OR
        // With [^ ]+, we any characters that are not a space.
        Pattern pattern = Pattern.compile("\"[^\"]*\"|'[^']*'|[^ ]+"); // This will let us "split" on whitespace while preserving quotes of all kinds!
        Matcher matcher = pattern.matcher(command);
        List<String> commandList = new ArrayList<>();
        while (matcher.find()) {
            commandList.add(matcher.group(0)); // Add all the matches to the list!
        }
        switch (commandList.get(0)) {
            case "ls":
                handleLs(commandList);
                break;
            case "cd":
                handleCd(commandList);
            break;
            case "delete":
                handleDelete(commandList);
            break;
            case "get":
                handleGet(commandList);
            break;
            case "put":
                handlePut(commandList);
            break;
            case "mkdir":
                handleMkdir(commandList);
            break;
            case "rmdir":
                handleRmdir(commandList);
            break;
            default:
                throw new CommandNotFoundException("Command not found! Your command: " + command);
        }
    }

    public static void handleLs(List<String> commandList) throws IOException {
        FTPFile[] currentDirectoryFiles;
        if (commandList.size() > 1) {
            System.out.println("Listing for: /" + commandList.get(1));
            currentDirectoryFiles = ftp.listFiles(commandList.get(1));
        }
        else {
            System.out.println("Listing for: " + ftp.printWorkingDirectory());
            currentDirectoryFiles = ftp.listFiles();
        }
        for (FTPFile file : currentDirectoryFiles) {
            System.out.println(file.getName() + (file.isDirectory() ? " (Directory)" : " (File)"));
        }
    }
    
    public static void handleCd(List<String> commandList) throws IOException, InvalidCommandException {
        if (commandList.size() > 1) {
            Map<String, FTPFile> files = Stream.of(ftp.listFiles()).collect(Collectors.toMap(FTPFile::getName, Function.identity()));
            String fileName = commandList.get(1);
            boolean inQuotes = fileName.substring(0, 1).equals("'") && fileName.substring(fileName.length() - 1, fileName.length()).equals("'");
            fileName = inQuotes ? fileName.substring(1, fileName.length() - 1) : fileName;
            if (files.containsKey(fileName) && files.get(fileName).isDirectory()) {
                if (fileName.equals("..")) {
                    System.out.println("Changing to parent directory...");
                    ftp.changeToParentDirectory();
                }
                else {
                    ftp.changeWorkingDirectory(fileName);
                    System.out.println("Changing to directory: " + ftp.printWorkingDirectory());
                }
            }
            else {
                System.err.println("Error: \"" + fileName + "\" is not a directory.");
            }
        }
        else {
            throw new InvalidCommandException("Invalid command; not enough arguments! Your command: " + commandList.stream().collect(Collectors.joining(" ")));
        }
    }
    
    public static void handleDelete(List<String> commandList) throws IOException, InvalidCommandException {
        if (commandList.size() > 1) {
            List<String> files = Stream.of(ftp.listFiles()).map(file -> file.getName()).collect(Collectors.toList());
            String fileName = commandList.get(1);
            boolean inQuotes = fileName.substring(0, 1).equals("'") && fileName.substring(fileName.length() - 1, fileName.length()).equals("'");
            fileName = inQuotes ? fileName.substring(1, fileName.length() - 1) : fileName;
            if (files.contains(fileName)) {
                if (ftp.deleteFile((inQuotes ? fileName.substring(1, fileName.length() - 1) : fileName))) {
                    System.out.println("File: " + fileName + " deleted!");
                }
                else {
                    System.err.println("File: \"" + fileName + "\" not deleted!");
                }
            }
            else {
                System.err.println("Delete failed. File: \"" + fileName + "\" does not exist!");
            }
        }
        else {
            throw new InvalidCommandException("Invalid command; not enough arguments! Your command: " + commandList.stream().collect(Collectors.joining(" ")));
        }
    }
    
    public static void handleGet(List<String> commandList) throws IOException, InvalidCommandException {
        if (commandList.size() > 1) {
            Map<String, FTPFile> files = Stream.of(ftp.listFiles()).collect(Collectors.toMap(FTPFile::getName, Function.identity()));
            String fileName = commandList.get(1);
            boolean inQuotes = fileName.substring(0, 1).equals("'") && fileName.substring(fileName.length() - 1, fileName.length()).equals("'");
            fileName = inQuotes ? fileName.substring(1, fileName.length() - 1) : fileName;
            if (files.containsKey(fileName)) {
                FTPFile file = files.get(fileName);
                if (file.isFile()) {
                    ftp.retrieveFile(fileName, new FileOutputStream(fileName));
                }
                else if (file.isDirectory()) {
                    File dir = new File(fileName);
                    dir.mkdir();
                    ftp.changeWorkingDirectory(fileName);
                    download(ftp.listFiles(), dir.getAbsolutePath());
                    ftp.changeToParentDirectory();
                }
                else {
                    System.err.println("Error. Not a file or directory!");
                }
            }
            else {
                System.err.println("\"" + fileName + "\" does not exist!");
            }
        }
        else {
            throw new InvalidCommandException("Invalid command; not enough arguments! Your command: " + commandList.stream().collect(Collectors.joining(" ")));
        }
    }

    public static void download(FTPFile[] files, String path) throws IOException {
        for (FTPFile file : files) {
            String newPath = path + File.separator + file.getName();
            if (file.isFile()) {
                ftp.retrieveFile(file.getName(), new FileOutputStream(newPath));
            }
            else if (file.isDirectory()) {
                File dir = new File(newPath);
                dir.mkdir();
                ftp.changeWorkingDirectory(file.getName());
                download(ftp.listFiles(), newPath);
                ftp.changeToParentDirectory();
            }
            else {
                System.err.println("Error. Not a file or directory!");
            }
        }
    }
    
    public static void handlePut(List<String> commandList) throws IOException, InvalidCommandException {
        if (commandList.size() > 1) {
            String fileName = commandList.get(1);
            boolean inQuotes = fileName.substring(0, 1).equals("'") && fileName.substring(fileName.length() - 1, fileName.length()).equals("'");
            fileName = inQuotes ? fileName.substring(1, fileName.length() - 1) : fileName;
            File localFile = new File(fileName);
            if (localFile.exists()) {
                if (localFile.isFile()) {
                    ftp.storeFile(fileName, new FileInputStream(localFile));
                }
                else if (localFile.isDirectory()) {
                    ftp.makeDirectory(fileName);
                    ftp.changeWorkingDirectory(fileName);
                    upload(localFile);
                    ftp.changeToParentDirectory();
                }
                else {
                    System.err.println("Error. Not a file or directory!");
                }
            }
            else {
                System.err.println("\"" + fileName + "\" does not exist!");
            }
        }
        else {
            throw new InvalidCommandException("Invalid command; not enough arguments! Your command: " + commandList.stream().collect(Collectors.joining(" ")));
        }
    }

    public static void upload(File aFile) throws IOException {
        for (File file : aFile.listFiles()) {
            if (file.isFile()) {
                ftp.storeFile(file.getName(), new FileInputStream(file));
            }
            else if (file.isDirectory()) {
                ftp.makeDirectory(file.getName());
                ftp.changeWorkingDirectory(file.getName());
                upload(file);
                ftp.changeToParentDirectory();
            }
            else {
                System.err.println("Error. Not a file or directory!");
            }
        }
    }
    
    public static void handleMkdir(List<String> commandList) throws IOException, InvalidCommandException {
        if (commandList.size() > 1) {
            List<String> directories = Stream.of(ftp.listDirectories()).map(dir -> dir.getName()).collect(Collectors.toList());
            String fileName = commandList.get(1);
            boolean inQuotes = fileName.substring(0, 1).equals("'") && fileName.substring(fileName.length() - 1, fileName.length()).equals("'");
            fileName = inQuotes ? fileName.substring(1, fileName.length() - 1) : fileName;
            if (!directories.contains(fileName)) {
                if (ftp.makeDirectory(fileName)) {
                    System.out.println("Made directory: \"" + fileName + "\"");
                }
                else {
                    System.err.println("Could not make directory: \"" + fileName + "\"!");
                }
            }
            else {
                System.err.println("Directory \"" + fileName + "\" already exists!");
            }
        }
        else {
            throw new InvalidCommandException("Invalid command; not enough arguments! Your command: " + commandList.stream().collect(Collectors.joining(" ")));
        }
    }
    
    public static void handleRmdir(List<String> commandList) throws IOException, InvalidCommandException {
        if (commandList.size() > 1) {
            List<String> directories = Stream.of(ftp.listDirectories()).map(dir -> dir.getName()).collect(Collectors.toList());
            String fileName = commandList.get(1);
            boolean inQuotes = fileName.substring(0, 1).equals("'") && fileName.substring(fileName.length() - 1, fileName.length()).equals("'");
            fileName = inQuotes ? fileName.substring(1, fileName.length() - 1) : fileName;
            if (directories.contains(fileName)) {
                ftp.changeWorkingDirectory(fileName);
                delete(ftp.listFiles());
                ftp.changeToParentDirectory();
                ftp.removeDirectory(fileName);
            }
            else {
                System.err.println("Directory \"" + fileName + "\" does not exist!");
            }
        }
        else {
            throw new InvalidCommandException("Invalid command; not enough arguments! Your command: " + commandList.stream().collect(Collectors.joining(" ")));
        }
    }

    public static void delete(FTPFile[] files) throws IOException {
        for (FTPFile file : files) {
            if (file.isFile()) {
                ftp.deleteFile(ftp.printWorkingDirectory() + (ftp.printWorkingDirectory().equals("/") ? "" : "/") + file.getName());
            }
            else if (file.isDirectory()) {
                ftp.changeWorkingDirectory(file.getName());
                if (ftp.listFiles().length != 0) {
                    delete(ftp.listFiles());;
                }
                ftp.changeToParentDirectory();
                ftp.removeDirectory(file.getName());
            }
            else {
                System.err.println("Error. Not a file or directory!");
            }
        }
    }
    
}