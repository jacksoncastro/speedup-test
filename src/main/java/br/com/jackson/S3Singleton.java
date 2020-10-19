package br.com.jackson;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.mediaconnect.model.NotFoundException;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;

public final class S3Singleton {

	private static final String BUCKET_NAME = "hipstershop-k6";
	
	private static AmazonS3 amazonS3;

	private S3Singleton() {
	}


	private static AmazonS3 getAmazonS3() {

		if (amazonS3 == null) {
			synchronized (S3Singleton.class) {
				if (amazonS3 == null) {
					amazonS3 = buildAmazonS3();
				}
			}
		}
		return amazonS3;
	}

	private static AmazonS3 buildAmazonS3() {

		String ACCESS_KEY = System.getenv(Constants.ENV_ACCESS_KEY);
		String SECRET_KEY = System.getenv(Constants.ENV_SECRET_KEY);

		AWSCredentials credentials = new BasicAWSCredentials(ACCESS_KEY, SECRET_KEY);

		AmazonS3 amazonS3 = AmazonS3ClientBuilder
				  .standard()
				  .withCredentials(new AWSStaticCredentialsProvider(credentials))
				  .withRegion(Regions.SA_EAST_1)
				  .build();

		if (!amazonS3.doesBucketExistV2(BUCKET_NAME)) {
			String message = String.format("Bucket with name %s don't exists", BUCKET_NAME);
			throw new NotFoundException(message);
		}

		return amazonS3;
	}

	public static String getItem(String key) {
		if (key == null) {
			return null;
		}
		return getAmazonS3().getObjectAsString(S3Singleton.BUCKET_NAME, key);
	}
}
