package com.redhat.cop.giveback;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.Lists;
import com.google.api.client.util.Maps;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.SheetsScopes;
import com.google.api.services.sheets.v4.model.ValueRange;

import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.GeneralSecurityException;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class ReadGoogleSheet {

    // Application name (can be anything)
    private static final String APPLICATION_NAME = "Google Sheets API Java Reader";

    // JSON Factory
    private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();

    // Directory to store user authorization tokens
    private static final String TOKENS_DIRECTORY_PATH = "tokens";

    // Scopes: We only need read-only access for this example.
    private static final List<String> SCOPES =
            Collections.singletonList(SheetsScopes.SPREADSHEETS_READONLY);

    // Path to the credentials.json file (must be in src/main/resources)
    private static final String CREDENTIALS_FILE_PATH = "/credentials.json";
    private static final String CREDENTIALS_SYSTEM_PROPERTY = "GOOGLE_SERVICE_ACCOUNT_CREDS";
    
    
    /**
     * Handles the OAuth 2.0 authorization flow.
     *
     * @param HTTP_TRANSPORT The HTTP transport instance.
     * @return An authorized Credential object.
     * @throws IOException If credentials file is not found or other I/O error.
     */
    private static Credential getCredentials(final NetHttpTransport HTTP_TRANSPORT) throws IOException {
        // Load client secrets from the credentials.json file
      
      String GOOGLE_SERVICE_ACCOUNT_CREDS=System.getProperty(CREDENTIALS_SYSTEM_PROPERTY);
      GoogleClientSecrets clientSecrets=GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(new ByteArrayInputStream(GOOGLE_SERVICE_ACCOUNT_CREDS.getBytes())));
      
//        InputStream in = ReadGoogleSheet.class.getResourceAsStream(CREDENTIALS_FILE_PATH);
//        if (in == null) {
//            throw new FileNotFoundException("Resource not found: " + CREDENTIALS_FILE_PATH);
//        }
//        GoogleClientSecrets clientSecrets =
//                GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));

        // Build the flow and trigger the user authorization request
        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, SCOPES)
                .setDataStoreFactory(new FileDataStoreFactory(new java.io.File(TOKENS_DIRECTORY_PATH)))
                .setAccessType("offline")
                .build();

        // This will open a browser window for the user to grant permission
        LocalServerReceiver receiver = new LocalServerReceiver.Builder().setPort(8888).build();
        
        // `authorize("user")` stores the token under the key "user" in the FileDataStore
        return new AuthorizationCodeInstalledApp(flow, receiver).authorize("user");
    }


    public List<Map<String,String>> readSheet(String spreadsheetId) throws FileNotFoundException,IOException{
        try {
            // ---!!! IMPORTANT: CHANGE THESE VALUES !!!---
            // The ID of the spreadsheet to read.
            // You can find this in the URL: https://docs.google.com/spreadsheets/d/THIS_IS_THE_ID/edit
//            final String spreadsheetId = "1E91hT_ZpySyvhnANxqZ7hcBSM2EEd9TqfQF-cavB8hQ";

            // The range to read, in A1 notation.
            // e.g., "Sheet1!A1:D10"
            final String range = "Form Responses 1!A1:D";
            // ---------------------------------------------


            // Build a new authorized API client service.
            final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
            
            // Get the credentials
            Credential credential = getCredentials(HTTP_TRANSPORT);

            // Build the Sheets service
            Sheets service = new Sheets.Builder(HTTP_TRANSPORT, JSON_FACTORY, credential)
                    .setApplicationName(APPLICATION_NAME)
                    .build();

            // Make the API call to get the values
            ValueRange response = service.spreadsheets().values()
                    .get(spreadsheetId, range)
                    .execute();

            // Get the list of rows
            List<List<Object>> values = response.getValues();

            List<Map<String,String>> result=Lists.newArrayList();
            if (values == null || values.isEmpty()) {
              System.out.println("No data found.");
            } else {
              System.out.println("Data found in " + spreadsheetId + " range " + range + ":");
              
              int iRow=0;
              Map<Integer,String> headerNames=Maps.newHashMap(); 
              for (List<Object> row : values) {
                  // Iterate over cells in the row
                  Map<String,String> m=Maps.newLinkedHashMap();
                  int iCol=0;
                  for (Object cell : row) {
//                      System.out.print(cell.toString() + "\t");
                      if (iRow==0) {
                        headerNames.put(iCol, cell.toString());
                      }else {
                        m.put(headerNames.get(iCol),cell.toString());
                      }
                      iCol+=1;
                  }
//                  System.out.println(); // New line after each row
                  if (iRow>0) result.add(m);
                  iRow+=1;
              }
            }
            
            return result;

        } catch (FileNotFoundException e) {
            System.err.println("ERROR: Could not find credentials.json.");
            System.err.println("Please follow the setup instructions in README.md.");
            throw e;
        } catch (IOException | GeneralSecurityException e) {
            System.err.println("An error occurred: " + e.getMessage());
            throw new IOException(e);
        } catch (Exception e) {
            System.err.println("An unexpected error occurred: " + e.getMessage());
            throw e;
        }
    }
}


