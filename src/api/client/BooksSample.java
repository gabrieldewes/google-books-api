package api.client;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.books.Books;
import com.google.api.services.books.Books.Volumes.List;
import com.google.api.services.books.model.Volume;
import com.google.api.services.books.model.Volumes;

import java.io.IOException;
import java.net.URLEncoder;
import java.text.NumberFormat;

public class BooksSample {
    private static final String APPLICATION_NAME = "myCompany-myApp-1.0";
    private static final NumberFormat CURRENCY_FORMATTER = NumberFormat.getCurrencyInstance();
    private static final NumberFormat PERCENT_FORMATTER = NumberFormat.getPercentInstance();

    private static void queryGoogleBooks(JsonFactory jsonFactory, String query) throws Exception {
        ClientCredentials.errorIfNotSpecified();

        final Books books = new Books.Builder(GoogleNetHttpTransport.newTrustedTransport(), jsonFactory, null)
                .setApplicationName(APPLICATION_NAME)
/* Se for específico para um usuário, autenticá-lo antes.
 *              .setGoogleClientRequestInitializer(new BooksRequestInitializer(ClientCredentials.API_KEY)) */
                .build();
        System.out.println("Query: [" + query + "]");
        List volumesList = books.volumes().list(query);
        // Filtrar somente os eBooks.
        //volumesList.setFilter("ebooks");
        Volumes volumes = volumesList.execute();
        if (volumes.getTotalItems() == 0 || volumes.getItems() == null) {
            System.out.println("No matches found.");
            return;
        }

        for (Volume volume : volumes.getItems()) {
            Volume.VolumeInfo volumeInfo = volume.getVolumeInfo();
            Volume.SaleInfo saleInfo = volume.getSaleInfo();
            System.out.println("==========");
            // ISBN
            java.util.List isbn = volumeInfo.getIndustryIdentifiers();
            if (isbn != null && !isbn.isEmpty()) {
                System.out.print("ISBN: ");
                for (int i=0; i<isbn.size(); i++) {
                    System.out.print(isbn.get(i));
                    if (i < isbn.size() - 1) {
                        System.out.print(", ");
                    }
                }
                System.out.println();
            }
            // Title.
            System.out.println("Title: " + volumeInfo.getTitle());
            // Author(s).
            java.util.List<String> authors = volumeInfo.getAuthors();
            if (authors != null && !authors.isEmpty()) {
                System.out.print("Author(s): ");
                for (int i = 0; i < authors.size(); ++i) {
                    System.out.print(authors.get(i));
                    if (i < authors.size() - 1) {
                        System.out.print(", ");
                    }
                }
                System.out.println();
            }
            // Description (if any).
            if (volumeInfo.getDescription() != null && volumeInfo.getDescription().length() > 0) {
                System.out.println("Description: " + volumeInfo.getDescription());
            }
            // Ratings (if any).
            if (volumeInfo.getRatingsCount() != null && volumeInfo.getRatingsCount() > 0) {
                int fullRating = (int) Math.round(volumeInfo.getAverageRating());
                System.out.print("User Rating: ");
                for (int i = 0; i < fullRating; ++i) {
                    System.out.print("*");
                }
                System.out.println(" (" + volumeInfo.getRatingsCount() + " rating(s))");
            }
            // Price (if any).
            if (saleInfo != null && "FOR_SALE".equals(saleInfo.getSaleability())) {
                double save = saleInfo.getListPrice().getAmount() - saleInfo.getRetailPrice().getAmount();
                if (save > 0.0) {
                    System.out.print("List: " + CURRENCY_FORMATTER.format(saleInfo.getListPrice().getAmount())
                            + "  ");
                }
                System.out.print("Google eBooks Price: "
                        + CURRENCY_FORMATTER.format(saleInfo.getRetailPrice().getAmount()));
                if (save > 0.0) {
                    System.out.print("  You Save: " + CURRENCY_FORMATTER.format(save) + " ("
                            + PERCENT_FORMATTER.format(save / saleInfo.getListPrice().getAmount()) + ")");
                }
                System.out.println();
            }
            // Access status.
            String accessViewStatus = volume.getAccessInfo().getAccessViewStatus();
            String message = "Informações adicionais e eBook disponíveis em:";
            if ("FULL_PUBLIC_DOMAIN".equals(accessViewStatus)) {
                message = "This public domain book is available for free from Google eBooks at:";
            } else if ("SAMPLE".equals(accessViewStatus)) {
                message = "Avaliação gratuita deste livro do Google eBooks em:";
            }
            System.out.println(message);
            // Link para Google eBooks.
            System.out.println(volumeInfo.getInfoLink());
        }
        System.out.println("==========");
        System.out.println(
                volumes.getTotalItems() + " total results at http://books.google.com/ebooks?q="
                        + URLEncoder.encode(query, "UTF-8"));
    }

    public static void main(String[] args) {
        JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
        args = new String[3];
        args[0] = "--author";
        args[1] = "--isbn";
        args[2] = "--title";
        try {
            if (args.length == 0) {
                System.err.println("Null pointer at Args");
                System.exit(1);
            }
            String prefix = null;
            String query = "";
            for (String arg : args) {
                if ("--author".equals(arg)) {
                    prefix = "inauthor:";
                }
                else if ("--isbn".equals(arg)) {
                    prefix = "isbn:";
                }
                else if ("--title".equals(arg)) {
                    prefix = "intitle:";
                }
                else if (arg.startsWith("--")) {
                    System.err.println("Argumento desconhecido: " + arg);
                    System.exit(1);
                }
                else {
                    query = arg;
                }
            }
            if (prefix != null) {
                query = prefix + query;
            }
            try {
                queryGoogleBooks(jsonFactory, query);
            } catch (IOException e) {
                System.err.println(e.getMessage());
            }
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }
}

