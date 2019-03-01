
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicSessionCredentials;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.rekognition.AmazonRekognition;
import com.amazonaws.services.rekognition.AmazonRekognitionClientBuilder;
import com.amazonaws.services.rekognition.model.CompareFacesRequest;
import com.amazonaws.services.rekognition.model.Image;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.ListObjectsRequest;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.amazonaws.util.IOUtils;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URL;
import java.nio.ByteBuffer;
import java.util.Map;
import javax.imageio.ImageIO;


@SuppressWarnings("Duplicates")
  public class SnowbirdLambda implements RequestHandler<String, String> {

    @Override
    public String handleRequest(String input, Context context) {

      try {
        getObject();
      } catch (Exception e) {
        e.printStackTrace();
      }


      return input + ": Success!";



    }

  public static void getObject() throws Exception {
    String dbName = "hertzsnowbird";
    String dbPath = "https://s3.amazonaws.com/hertzsnowbird/";
    String rekogName = "hertzsnowbirddeeplens";
    String rekogPath = "https://s3.amazonaws.com/hertzsnowbirddeeplens/";

    BasicSessionCredentials basicSessionCredentials = new BasicSessionCredentials(
        "AKIAJB6GX5VF77QRCWIA",
        "sxPP9ss+tdIG5vmyDVg2rajXFPrES2TwJdVaSpzOG","");

    AmazonS3 s3client = AmazonS3ClientBuilder.standard().withRegion("us-east-1")
        .build();

    ListObjectsRequest listObjectsRequest = new ListObjectsRequest()
        .withBucketName(dbName);
    ObjectListing objectListing;

    do {
      objectListing = s3client.listObjects(listObjectsRequest);
      for (S3ObjectSummary objectSummary :
          objectListing.getObjectSummaries()) {
        ByteBuffer sourceImageBytes = null;
        ByteBuffer targetImageBytes = null;
        URL sourceURL = new URL(dbPath + objectSummary.getKey());
        URL targetURL = new URL("https://s3.amazonaws.com/hertzsnowbirddeeplens/deeptest.jpg");

        BufferedImage sourceImg = ImageIO.read(sourceURL);
        File sourceFile = new File("files/sourceImg.jpg");
        sourceFile.createNewFile();
        ImageIO.write(sourceImg, "sourceImg", sourceFile);

        BufferedImage targetImg = ImageIO.read(targetURL);
        File targetFile = new File("files/targetImg.jpg");
        ImageIO.write(targetImg, "targetImg", targetFile);

        //Load source and target images and create input parameters
        InputStream inputStream = new FileInputStream(sourceFile);

        sourceImageBytes = ByteBuffer.wrap(IOUtils.toByteArray(inputStream));

        InputStream inputStream2 = new FileInputStream(targetFile);
        targetImageBytes = ByteBuffer.wrap(IOUtils.toByteArray(inputStream2));

        System.out.println();

        Image source = new Image()
            .withBytes(sourceImageBytes);
        Image target = new Image()
            .withBytes(targetImageBytes);
        Float similarityThreshold = 70F;

        CompareFacesRequest request = new CompareFacesRequest()
            .withSourceImage(source)
            .withTargetImage(target)
            .withSimilarityThreshold(similarityThreshold);

        AmazonRekognition rekognitionClient = AmazonRekognitionClientBuilder.standard()
            .withCredentials(new AWSStaticCredentialsProvider(basicSessionCredentials))
            .withRegion("us-east-1").build();
        ProfileCredentialsProvider profileCredentialsProvider = new ProfileCredentialsProvider();



        // Call compare face results. From this you will see percent similarity. Based on the threshold you can confirm if
        // the person in the camera is one from one in the database.
        //|
        //v

        /**CompareFacesResult compareFacesResult = rekognitionClient.compareFaces(request);*/


        // can not get api call for Salesforce due to time constraints
        //but we do have a salesforce database that has diver information to query from


        //for demo purposes we will assume that Einstein determined that the customer prefers New Base model Ford Mustangs
        //and the natural upgrade that the user might be interested in is a Ford Mustangs

        String matchedCar = "2019 Ford Mustang at $150/day";
        String upgradedCar = "2019 Ford Mustang GT  with V8 Turbo Engine for a $15 premium per day";

        String returnString = "Hello Kamp! There is a "+matchedCar+" in this Gold Line. There is also a "+upgradedCar;

        System.out.println(returnString);

      }
      listObjectsRequest.setMarker(objectListing.getNextMarker());
    } while (objectListing.isTruncated());
  }




}
