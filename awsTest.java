import java.util.Scanner;
import java.util.ArrayList;
import java.util.Arrays;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.model.DescribeInstancesRequest;
import com.amazonaws.AmazonClientException;
import com.amazonaws.services.ec2.model.DescribeInstancesResult;
import com.amazonaws.services.ec2.model.Reservation;
import com.amazonaws.services.ec2.model.Instance;
import com.amazonaws.services.ec2.AmazonEC2ClientBuilder;
import com.amazonaws.services.ec2.model.InstanceType;
import com.amazonaws.services.ec2.model.RunInstancesRequest;
import com.amazonaws.services.ec2.model.RunInstancesResult;
import com.amazonaws.services.ec2.model.Tag;
import com.amazonaws.services.ec2.model.CreateTagsRequest;
import com.amazonaws.services.ec2.model.CreateTagsResult;
import com.amazonaws.services.ec2.model.StartInstancesRequest;
import com.amazonaws.services.ec2.model.DryRunResult;
import com.amazonaws.services.ec2.model.DryRunSupportedRequest;
import com.amazonaws.services.ec2.model.StopInstancesRequest;
import com.amazonaws.services.ec2.model.RebootInstancesRequest;
import com.amazonaws.services.ec2.model.RebootInstancesResult;

//리전 및 가용 영역
import com.amazonaws.services.ec2.model.DescribeRegionsResult;
import com.amazonaws.services.ec2.model.Region;
import com.amazonaws.services.ec2.model.AvailabilityZone;
import com.amazonaws.services.ec2.model.DescribeAvailabilityZonesResult;

import com.amazonaws.services.ec2.model.Filter;
import com.amazonaws.services.ec2.model.DescribeImagesRequest;
import com.amazonaws.services.ec2.model.DescribeImagesResult;

public class awsTest {

	static AmazonEC2 ec2;
	private static void init() throws Exception {

		ProfileCredentialsProvider credentialsProvider = new ProfileCredentialsProvider();
		try {
		credentialsProvider.getCredentials();
		} catch (Exception e) {
			throw new AmazonClientException(
			"Cannot load the credentials from the credential profiles file. " +
			"Please make sure that your credentials file is at the correct " +
			"location (~/.aws/credentials), and is in valid format.",
			e);
		}
		ec2 = AmazonEC2ClientBuilder.standard()
			.withCredentials(credentialsProvider)
			.withRegion("us-west-2") /* check the region at AWS console */
			.build();
	}
	
	public static void main(String[] args) throws Exception {
		init();
		Scanner menu = new Scanner(System.in);
		Scanner id_string = new Scanner(System.in);
		int number = 0;
		while(true)
		{
			System.out.println("                                                            ");
			System.out.println("                                                            ");
			System.out.println("------------------------------------------------------------");
			System.out.println("           Amazon AWS Control Panel using SDK               ");
			System.out.println("                                                            ");
			System.out.println("  Cloud Computing, Computer Science Department              ");
			System.out.println("                           at Chungbuk National University  ");
			System.out.println("------------------------------------------------------------");
			System.out.println("  1. list instance                2. available zones         ");
			System.out.println("  3. start instance               4. available regions      ");
			System.out.println("  5. stop instance                6. create instance        ");
			System.out.println("  7. reboot instance              8. list images            ");
			System.out.println("                                 99. quit                   ");
			System.out.println("------------------------------------------------------------");
			System.out.print("Enter an integer: ");
			number = menu.nextInt();
			switch(number) {
			case 1: 
				listInstances();
				break;
			case 2: 
				availableZones();
				break;
			case 3: 
				startInstance();
				break;
			case 4: 
				availableRegions();
				break;
			case 5: 
				stopInstance();
				break;
			case 6: 
				createInstance();
				break;
			case 7: 
				rebootInstance();
				break;
			case 8: 
				listImages();
				break;
			case 99:
			default:
				System.exit(0);
			}
		}
	}
	
	
	
	//1. 인스턴스 리스트
	public static void listInstances()
	{
		System.out.println("Listing instances...");
		boolean done = false;
		DescribeInstancesRequest request = new DescribeInstancesRequest();
		while(!done) {
			DescribeInstancesResult response = ec2.describeInstances(request);
			for(Reservation reservation : response.getReservations()) {
				for(Instance instance : reservation.getInstances()) {
					System.out.printf(
					"[id] %s, " +
					"[AMI] %s, " +
					"[type] %s, " +
					"[state] %10s, " +
					"[monitoring state] %s",
					instance.getInstanceId(),
					instance.getImageId(),
					instance.getInstanceType(),
					instance.getState().getName(),
					instance.getMonitoring().getState());
				}
				System.out.println();
			}
			request.setNextToken(response.getNextToken());
			if(response.getNextToken() == null) {
				done = true;
			}
		}
	}
	
	
	
	//2. 가용 영역
	public static void availableZones()
	{
		final AmazonEC2 ec2 = AmazonEC2ClientBuilder.defaultClient();

		DescribeAvailabilityZonesResult zones_response =
		    ec2.describeAvailabilityZones();

		System.out.println("listing available zones...");
		for(AvailabilityZone zone : zones_response.getAvailabilityZones()) {
		    System.out.printf(
		        "[zone] %-15s " +
		        "[status] %-15s " +
		        "[region] %s",
		        zone.getZoneName(),
		        zone.getState(),
		        zone.getRegionName() + "\n");
		}
	}
	
	
	
	//3. 인스턴스 시작
	public static void startInstance()
	{
	
		Scanner sc = new Scanner(System.in);
		System.out.print("Please enter instance id : ");
		String instance_id = sc.next();
		
		final AmazonEC2 ec2 = AmazonEC2ClientBuilder.defaultClient();

		DryRunSupportedRequest<StartInstancesRequest> dry_request =
		    () -> {
		    StartInstancesRequest request = new StartInstancesRequest()
		        .withInstanceIds(instance_id);

		    return request.getDryRunRequest();
		};

		DryRunResult dry_response = ec2.dryRun(dry_request);

		if(!dry_response.isSuccessful()) {
		    System.out.printf(
		        "Failed dry run to start instance %s", instance_id);

		    throw dry_response.getDryRunResponse();
		}

		StartInstancesRequest request = new StartInstancesRequest()
		    .withInstanceIds(instance_id);

		ec2.startInstances(request);

		System.out.printf("Successfully started instance %s", instance_id);
	}
	
	
	
	//4. 가용 리전
	public static void availableRegions()
	{
		final AmazonEC2 ec2 = AmazonEC2ClientBuilder.defaultClient();

		DescribeRegionsResult regions_response = ec2.describeRegions();

		System.out.println("listing avaiable regions...");
		for(Region region : regions_response.getRegions()) {
		    System.out.printf(
		        "[region] %-15s " +
		        "[endpoint] %s",
		        region.getRegionName(),
		        region.getEndpoint() + "\n");
		}
	}
	
	
	
	//5. 인스턴스 중지
	public static void stopInstance()
	{
	
		Scanner sc = new Scanner(System.in);
		System.out.print("Please enter instance id : ");
		String instance_id = sc.next();
		
		final AmazonEC2 ec2 = AmazonEC2ClientBuilder.defaultClient();

		DryRunSupportedRequest<StopInstancesRequest> dry_request =
		    () -> {
		    StopInstancesRequest request = new StopInstancesRequest()
		        .withInstanceIds(instance_id);

		    return request.getDryRunRequest();
		};

		DryRunResult dry_response = ec2.dryRun(dry_request);

		if(!dry_response.isSuccessful()) {
		    System.out.printf(
		        "Failed dry run to stop instance %s", instance_id);
		    throw dry_response.getDryRunResponse();
		}

		StopInstancesRequest request = new StopInstancesRequest()
		    .withInstanceIds(instance_id);

		ec2.stopInstances(request);

		System.out.printf("Successfully stop instance %s", instance_id);
	}

	
	
	//6. 인스턴스 생성
	public static void createInstance()
    	{
	    	Scanner sc = new Scanner(System.in);
		System.out.print("Please enter instance name : ");
		String name = sc.next();
		System.out.print("Please Enter ami id : ");
		String ami_id = sc.next();

		RunInstancesRequest run_request = new RunInstancesRequest()
		    .withImageId(ami_id)
		    .withInstanceType(InstanceType.T1Micro)
		    .withMaxCount(1)
		    .withMinCount(1);

		RunInstancesResult run_response = ec2.runInstances(run_request);

		String reservation_id = run_response.getReservation().getInstances().get(0).getInstanceId();

		Tag tag = new Tag()
		    .withKey("Name")
		    .withValue(name);

		CreateTagsRequest tag_request = new CreateTagsRequest()
		    .withResources(reservation_id)
		    .withTags(tag);

		CreateTagsResult tag_response = ec2.createTags(tag_request);

		System.out.printf(
		    "Successfully started EC2 instance %s based on AMI %s",
		    reservation_id, ami_id);
    	}
    	
    	
    	
    	//7. 인스턴스 재부팅
    	public static void rebootInstance()
    	{
	    	Scanner sc = new Scanner(System.in);
		System.out.print("Please enter instance id : ");
		String instance_id = sc.next();

		final AmazonEC2 ec2 = AmazonEC2ClientBuilder.defaultClient();

		RebootInstancesRequest request = new RebootInstancesRequest()
		    .withInstanceIds(instance_id);

		RebootInstancesResult response = ec2.rebootInstances(request);

		System.out.printf(
		    "Successfully rebooted instance %s", instance_id);
    	}
    	
    	
    	
    	//8. 이미지 리스트
    	public static void listImages()
    	{
    		Filter filter = new Filter();
		filter.setName("owner-id");
		String owner_id = "892912192465";
		filter.setValues(Arrays.asList(owner_id));
		DescribeImagesRequest request = new DescribeImagesRequest().withFilters(filter);
		DescribeImagesResult describeImagesResult = ec2.describeImages(request);

		System.out.println("listing images...");
		describeImagesResult.getImages().forEach(image -> {
			System.out.printf(
					"[id] %s, " +
					"[name] %s, " +
					"[type] %s, " +
					"[state] %s, " +
					"[creation date] %s",
					image.getImageId(),
					image.getName(),
					image.getImageType(),
					image.getState(),
					image.getCreationDate());
		});
    	}
    	
    	
}
