import java.io.*;

public record DataFile(String type) {

    //constructor
    public DataFile(String type) {
        this.type = getUserInput(type);
    }

    //this method get the name of the file that needs to be loaded into the application
    public String getUserInput(String fileType) {
        //Ask user to enter the file name needed
        System.out.println("Please, enter the name of the .csv file with the " + fileType + "' details (without the .csv extension):");
        BufferedReader input = new BufferedReader(new InputStreamReader(System.in));
        String file = null;
        try {
            file = input.readLine();
        } catch (IOException e) {
            System.out.println("Error: name of the file is invalid!");
        }
        return file;
    }

    //accessor method for type
    public String getFileName() {
        return type;
    }

}