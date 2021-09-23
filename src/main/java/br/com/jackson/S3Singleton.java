package br.com.jackson;

import java.io.File;
import java.nio.file.Path;
import java.util.List;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.mediaconnect.model.NotFoundException;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.S3ObjectSummary;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class S3Singleton {

	// TODO: allow to pick up dynamically
	// private static final String BUCKET_NAME = "hipstershop-k6";
	private static final String BUCKET_NAME = "blueperf-k6";

	private static final String FORMAT_REGEX_ROUND = ".*\\/[a-zA-Z]+\\+?\\/[a-z]+-%d\\.[a-z]{3,4}";

	private static AmazonS3 amazonS3;

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

		String accessKey = System.getenv(Constants.ENV_ACCESS_KEY);
		String secretKey = System.getenv(Constants.ENV_SECRET_KEY);

		AWSCredentials credentials = new BasicAWSCredentials(accessKey, secretKey);

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

	public static boolean existsItem(String key) {
		if (key == null) {
			return false;
		}
		return getAmazonS3().doesObjectExist(BUCKET_NAME, key);
	}

	public static void deleteRound(String prefix, int round) {

		if (prefix == null || round < 1) {
			return;
		}

		String regex = String.format(FORMAT_REGEX_ROUND, round);

		List<S3ObjectSummary> summaries = getAmazonS3()
				.listObjectsV2(BUCKET_NAME, prefix + "/")
				.getObjectSummaries();

		summaries.stream()
			.map(S3ObjectSummary::getKey)
			.filter(key -> key.matches(regex))
			.forEach(key -> {
				log.info("Deleting file {}", key);
				getAmazonS3().deleteObject(BUCKET_NAME, key);
			});
	}

	public static void deleteFolder(String folder) {

		ObjectListing objects = getAmazonS3().listObjects(BUCKET_NAME, folder + "/");
		for (S3ObjectSummary objectSummary : objects.getObjectSummaries())  {
			getAmazonS3().deleteObject(BUCKET_NAME, objectSummary.getKey());
		}
	}

	public static void uploadFile(Path path, File file) {
		int attempt = 10;
		while (attempt >= 0) {
			try {
				log.info("Uploading file: " + path.toString());
				getAmazonS3().putObject(BUCKET_NAME, path.toString(), file);
				log.info("Upload successful");
				break;
			} catch (Exception e) {
				log.error("Failed to attempt file upload:" + path.toString(), e);
				attempt--;
				FunctionHelper.sleep(5);
			}
		}
		if (attempt < 0) {
			throw new RuntimeException("Fail to upload file: " + path.toString());
		}
	}
}
