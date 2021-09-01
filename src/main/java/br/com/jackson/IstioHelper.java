package br.com.jackson;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import com.amazonaws.services.cloudwatch.model.InvalidFormatException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

import br.com.jackson.dto.istio.Delay;
import br.com.jackson.dto.istio.Fault;
import br.com.jackson.dto.istio.VirtualServiceKind;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class IstioHelper {

	public static final int ONE_HUNDRED_PERCENT_FAULT = 100;

	// Format: 1h/1m/1s/1ms. MUST be >=1ms.
	private static final String REGEX_DELAY_VIRTUAL_SERVICE = "^[1-9][0-9]*(h|m|s|ms)$";

    private static final String FORMAT_GET_VIRTUAL_SERVICE = "kubectl get vs/%s -o yaml";

    // set fault

    public static void setFaultAllVirtualServices(int percent, String duration) {
        List<String> virtualServicesNames =  getVirtualServicesNames();
        for (String virtualServiceName : virtualServicesNames) {
            setFaultVirtualService(virtualServiceName, percent, duration);
        }
    }

    public static void setFaultAllVirtualServicesButTarget(String name, int percent, String duration) {
        List<String> virtualServicesNames =  getVirtualServicesNames();
        for (String virtualServiceName : virtualServicesNames) {
            if (!virtualServiceName.equalsIgnoreCase(name)) {
                setFaultVirtualService(virtualServiceName, percent, duration);
            }
        }
    }

    public static void setFaultVirtualService(String name, int percent, String duration) {
        VirtualServiceKind virtualService = getVirtualService(name);
        cleanFault(virtualService);

        Fault fault = createFault(percent, duration);
        virtualService.getSpec().getHttp().get(0).setFault(fault);
        applyVirtualService(virtualService);
    }

    // unset fault

    public static void unsetFaultAllVirtualServices() {
        List<String> virtualServicesNames =  getVirtualServicesNames();
        for (String virtualServiceName : virtualServicesNames) {
            unsetFaultVirtualService(virtualServiceName);
        }
    }

    private static void unsetFaultVirtualService(String name) {
        VirtualServiceKind virtualService = getVirtualService(name);
        cleanFault(virtualService);
        applyVirtualService(virtualService);
    }

    private static List<String> getVirtualServicesNames() {
        return FuntionHelper.exec("kubectl get vs -o NAME | awk -F/ '{print $2}'");
    }

    private static void cleanFault(VirtualServiceKind virtualService) {
        virtualService.getSpec()
            .getHttp().parallelStream()
                .filter(http -> http.getFault() != null)
                .forEach(http -> {
                    http.setFault(null);
                });
    }

    private static void checkDelay(String delay) {
    	if (!delay.matches(REGEX_DELAY_VIRTUAL_SERVICE)) {
    		throw new InvalidFormatException("Format: 1h/1m/1s/1ms. MUST be >=1ms. Value given: " + delay);
    	}
    }

    private static Fault createFault(int percent, String duration) {
    	checkDelay(duration);
        return new Fault(new Delay(percent, duration));
    }

    private static VirtualServiceKind getVirtualService(String name) {
        try {
        	String command = String.format(FORMAT_GET_VIRTUAL_SERVICE, name);
            List<String> exec = FuntionHelper.exec(command);
            String yaml = exec.stream().collect(Collectors.joining("\n"));
            ObjectMapper objectMapper = new ObjectMapper(new YAMLFactory());
            objectMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
            return objectMapper.readValue(yaml, VirtualServiceKind.class);
        } catch (IOException e) {
        	throw new RuntimeException(e);
		}
    }

    private static void applyVirtualService(VirtualServiceKind virtualService) {
        try {
            File file = File.createTempFile("virtual-service-", ".yaml");
            try {
                ObjectMapper objectMapper = new ObjectMapper(new YAMLFactory());
                objectMapper.writeValue(file, virtualService);
                FuntionHelper.exec("kubectl apply -f " + file.getAbsolutePath());
            } finally {
                file.deleteOnExit();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
