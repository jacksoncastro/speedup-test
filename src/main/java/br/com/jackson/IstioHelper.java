package br.com.jackson;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

import br.com.jackson.dto.istio.Delay;
import br.com.jackson.dto.istio.Fault;
import br.com.jackson.dto.istio.Http;
import br.com.jackson.dto.istio.Route;
import br.com.jackson.dto.istio.VirtualServiceKind;
import br.com.jackson.dto.kubernates.ListKind;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class IstioHelper {

	public static final int ONE_HUNDRED_PERCENT_FAULT = 100;

	// Format: 1h/1m/1s/1ms. Allow fraction like: 0.025s. MUST be >=1ms.
	private static final String REGEX_DELAY_VIRTUAL_SERVICE = "^(0\\.\\d*)*[1-9][0-9]*(h|m|s|ms)$";

    // set fault

	public static void setFaultAllVirtualServices(int percent, String duration) {
		Predicate<? super Http> predicate = http -> true;
		getAllVirtualServices().getItems()
			.parallelStream()
			.forEach(service -> {
				setFaultByDestiny(service, percent, duration, predicate);
			});
	}

    public static void setFaultAllVirtualServicesButTarget(String target, int percent, String duration) {
		Predicate<? super Http> predicate = http -> !routersContainsTarget(http.getRoute(), target);
		getAllVirtualServices().getItems()
			.parallelStream()
			.forEach(service -> {
				setFaultByDestiny(service, percent, duration, predicate);
			});
	}

	public static void setFaultVirtualService(String name, int percent, String duration) {
		Predicate<? super Http> predicate = http -> routersContainsTarget(http.getRoute(), name);
		getAllVirtualServices().getItems()
			.parallelStream()
			.forEach(service -> {
				setFaultByDestiny(service, percent, duration, predicate);
			});
	}

	private static void setFaultByDestiny(VirtualServiceKind virtualService, int percent, String duration, Predicate<? super Http> predicate) {
		final AtomicBoolean hasChanges = new AtomicBoolean(false);
		virtualService.getSpec()
			.getHttp()
			.parallelStream()
			.filter(predicate)
			.forEach(http -> {
				synchronized (hasChanges) {
					Fault fault = createFault(percent, duration);
					http.setFault(fault);
					hasChanges.set(true);
				}
			});
		if (hasChanges.get()) {
			applyVirtualService(virtualService);
		}
	}

	private static void checkDelay(String delay) {
    	if (!delay.matches(REGEX_DELAY_VIRTUAL_SERVICE)) {
    		throw new IllegalArgumentException("Format: 1h/1m/1s/1ms. MUST be >=1ms. Value given: " + delay);
    	}
    }

    private static Fault createFault(int percent, String duration) {
    	checkDelay(duration);
        return new Fault(new Delay(percent, duration));
    }

	public static void cleanAllFault() {
    	getAllVirtualServices().getItems()
    		.parallelStream()
    		.forEach(virtualService -> {
    			cleanFault(virtualService);
    			applyVirtualService(virtualService);
    		});
	}

	private static void applyVirtualService(VirtualServiceKind virtualService) {
        try {
            File file = File.createTempFile("virtual-service-", ".yaml");
            try {
                ObjectMapper objectMapper = new ObjectMapper(new YAMLFactory());
                objectMapper.writeValue(file, virtualService);
                FunctionHelper.exec("kubectl apply -f " + file.getAbsolutePath());
            } finally {
                file.deleteOnExit();
            }
        } catch (IOException e) {
            throw new RuntimeException("Fail on apply Virtual Service", e);
        }
    }

    private static void cleanFault(VirtualServiceKind virtualService) {
        virtualService.getSpec()
            .getHttp()
            .parallelStream()
            .filter(http -> http.getFault() != null)
            .forEach(http -> {
                http.setFault(null);
            });
    }

	private static boolean routersContainsTarget(List<Route> routes, String hostName) {
		if (routes == null) {
			return false;
		}
		return routes.parallelStream().anyMatch(route -> route.getDestination().getHost().equalsIgnoreCase(hostName));
	}

	private static ListKind<VirtualServiceKind> getAllVirtualServices() {
		try {
			List<String> virtualServicesNames = FunctionHelper.exec("kubectl get vs -o yaml", false);
			String yaml = virtualServicesNames.stream().collect(Collectors.joining("\n"));
	        ObjectMapper objectMapper = new ObjectMapper(new YAMLFactory());
	        objectMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
	        return objectMapper.readValue(yaml, new TypeReference<ListKind<VirtualServiceKind>>(){});
		} catch (Exception e) {
			throw new RuntimeException("Fail on get all Virtual Services", e);
		}
	}
}
