// Austin P
// Program returns ticket details (individually or in a list) using the [REDACTED] Request API

package zcc.TicketViewer;

import okhttp3.*;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;

import java.io.IOException;
import java.util.Base64;
import java.util.Scanner;

public class TicketGetter {
    private String content; //Will hold json
    private String subdomain;
    private String email;
    private String password;
    private String credentials;
    private int responseCode;
    OkHttpClient client; // Client instance

    TicketGetter(String subdomain, String email, String password) { // Constructor
        this.subdomain = subdomain;
        this.email = email;
        this.password = password;
        credentials = email + ":" + password;
        credentials = Base64.getEncoder().encodeToString(credentials.getBytes());
        client = new OkHttpClient().newBuilder().build();
    }
    public int getResponseCode()
    {
        return responseCode;
    }
    void getQueryContents(String query) throws IOException { // Access url and store response, also returns html response code

        Request req = new Request.Builder().url(query).addHeader("Authorization", "Basic " + credentials).build();
        Response resp = client.newCall(req).execute();
        ResponseBody respBod = resp.body();

        responseCode = resp.code();
        if (responseCode > 400&& responseCode<500)
        {
            System.out.println("There is an error with the request! Check your input again");
            System.out.println("Error Code: "+responseCode);
            if (responseCode == 401)
            {
                System.out.println("Authorization Failed! Check your credentials");
            }
            else if (responseCode == 404)
            {
                System.out.println("There is nothing in the records that matches your request!");
            }
        }
        else if (responseCode>=500)
        {
            System.out.println("Server is having trouble! Perhaps the API is down?");
            System.out.println("Error Code: "+responseCode);
        }
        if (respBod != null) {
            content = respBod.string();
        } else {
            System.out.println("Error! Content could not be found");
        }
    }

    String returnJSONfromURL(String query) throws IOException {
        Request req = new Request.Builder().url(query).addHeader("Authorization", "Basic " + credentials).build();
        Response resp = client.newCall(req).execute();
        ResponseBody respBod = resp.body();
        //System.out.println(respBod.string());
        return respBod.string();
    }

    void printTicket() throws IOException { // Print out relevant ticket details
        int datumIndex = 0;

        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode contentObj = objectMapper.readTree(content);
        JsonNode data = contentObj.get("request");

        if (data != null) {

            for (JsonNode dataNode : data) {
                String nodeText = dataNode.asText(); // Will hold our current element

                switch (datumIndex) { // This block is responsible for displaying out the ticket details
                                    // Each case corresponds to a field in the ticket
                    case 1:
                        System.out.println("Ticket ID:" + nodeText);
                        break;
                    case 2:
                        System.out.println("Status: " + nodeText);
                        break;
                    case 5:
                        System.out.println("Subject: " + nodeText);
                        break;
                    case 6:
                        System.out.println("Description: " + nodeText + "\n---------------------------------------");
                        break;
                    case 7:
                        System.out.println("Organization ID: " + nodeText);
                        break;
                    case 10:
                        System.out.println("Requester ID: " + nodeText + "\n---------------------------------------");
                        break;
                    case 16:
                        System.out.println("Created At: " + nodeText);
                        break;
                    case 17:
                        System.out.println("Last Updated At: " + nodeText);
                        break;
                    case 20:
                        System.out.println("Assigned Agent ID: " + nodeText);
                        System.out.println("########################################");
                        break;
                }
                datumIndex++; // Keep track of current position in ticket
            }
        } else {
            System.out.println("No ticket matches that ID");
        }
        System.out.println(" ");
    }

    void getTicketDetails(int id) throws IOException {
        String url = "https://" + subdomain + ".REDACTED.com/api/v2/requests/" + id + ".json";
        getQueryContents(url);
        printTicket();
    }

    void parseTicketList() throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode contentObj = objectMapper.readTree(content);

        int datumIndex = 0; // Will hold index of current element # in current json
        int ticketIndex = 0; // Will hold index of current ticket

        int userChoice = 0; // User answer to go back or forth
        int prevBookmark = 0; // Keeps bookmark of Previous Page's starting index

        boolean bypassPrompt = false;
        JsonNode data = contentObj.get("requests"); // Should have entire list of tickets
        if (data != null) {
            boolean moribund = false;
            while (!moribund) {
                // Should have used the API's pagination given more time but...
                // incrementing / decrementing a number seems easier than implementing more query requests to me
                if (!bypassPrompt) // Deals with edge case after reaching end of ticket list
                {
                    if ((ticketIndex % 25 == 0) && (ticketIndex != 0)) {
                        System.out.println("Enter 1 to view the previous 25 tickets");
                        System.out.println("Enter 2 to view the next 25 tickets");
                        System.out.println("Enter 3 to Exit to main menu");
                        userChoice = promptAnswer();
                        if (userChoice == 1) // user entered 1 for previous
                        {
                            if (ticketIndex == 25)// If on first "page" then just redisplay first page
                            {
                                prevBookmark = 0; // So back to first item
                            } else // Else we go to the page before the one we just showed
                            {
                                prevBookmark -= 25; // So back 25 tickets
                            }
                            ticketIndex = prevBookmark; // Change ticketIndex to prevBookmark
                        } else if (userChoice == 2) // User chose to go to the next page
                        {
                            prevBookmark += 25; // Update accordingly
                        } else if (userChoice == 3) {
                            break;
                        }
                    }// Pagination Implementation Ends Here
                } else // If the above was bypassed, use this block
                {
                    bypassPrompt = false; // Toggle bypassPrompt from true to false
                    prevBookmark -= 25;
                    ticketIndex = prevBookmark;
                }
                JsonNode ticketDatum = contentObj.get("requests").get(ticketIndex); // Get the initial json from api
                if (ticketDatum != null)
                {
                    for (JsonNode ticketElement : ticketDatum) // iterate through each element in json
                    {
                        if (datumIndex == 1) //Found ID for current ticket
                        {
                            System.out.println("========================================");
                            System.out.println("\t\t\tTicket Index #" + ticketIndex);
                            System.out.println("========================================");
                            ticketIndex++; // Increment to prepare for next ticket
                            getTicketDetails(ticketElement.asInt()); // Reuse getTicketDetails function
                                                                    // by using ID we found
                        }
                        // This block determines which line of the ticket the program is currently inspecting
                        if (datumIndex != 22)  // Still on current ticket
                        {
                            datumIndex++;
                        } else  // Next ticket reached, restart index
                        {
                            datumIndex = 0;
                        }

                    }
                } else {
                    System.out.println("You have reached the end of the ticket list!");
                    System.out.println("Enter 1 to view the previous page");
                    System.out.println("Enter 2 to anger the program (Exit)");
                    System.out.println("Enter 3 to Exit to the main menu");
                    userChoice = promptAnswer();
                    if (userChoice == 3)
                    moribund=true; //Exit while loop
                    else if (userChoice == 1)
                    {
                        bypassPrompt = true;
                    }
                    else
                    {
                        System.out.println(">:( Good bye!");
                        break; // Essentially just another exit,
                    }
                }
            }
        } else {
            System.out.println("No ticket matches that ID");
        }
        System.out.println("Exiting to main menu");
    }

    void viewAllTickets() throws IOException {
        String url = "https://" + subdomain + ".REDACTED.com/api/v2/requests.json";
        getQueryContents(url);
        parseTicketList();
    }

    public int promptAnswer() {
        Scanner scan = new Scanner(System.in);
        int userInput = 0;
        while (!scan.hasNextInt()) {
            System.out.println("Please input a valid INTEGER instead");
            scan.next();
        }
        userInput = Math.abs(scan.nextInt()); // Ensures all answers are positive integers
        return userInput;
    }


    public static void main(String[] args) throws IOException {
        Scanner scan = new Scanner(System.in); // user input
        String email = "";
        String pw = "";
        String subdomain = "";

        System.out.println("Enter the SubDomain [EX: zccInternApplicant]");
        subdomain = scan.next();
        System.out.println("Enter email [EX: user@gmail.com]");
        email = scan.next();
        System.out.println("Enter the password");
        pw = scan.next();

        TicketGetter tg = new TicketGetter(subdomain, email, pw); // Instantiate class for method access
        boolean moribund = false;
        int userChoice = 0;
        while (!moribund) {
            System.out.println("Please enter the corresponding number to pick an option");
            System.out.println("1 - Search a specific ticket using ticket ID number");
            System.out.println("2 - List tickets");
            System.out.println("3 - Exit");

            userChoice = tg.promptAnswer();

            if (userChoice == 1) {
                System.out.println("What is the Ticket's ID #?");
                userChoice = tg.promptAnswer();
                tg.getTicketDetails(userChoice);
            } else if (userChoice == 2) {
                System.out.println("Loading ticket info from json...");
                tg.viewAllTickets();
            } else {
                System.out.println("User chose to Exit Program...");
                moribund = true;
            }
        }
        System.out.println("Goodbye....");
    }
}
