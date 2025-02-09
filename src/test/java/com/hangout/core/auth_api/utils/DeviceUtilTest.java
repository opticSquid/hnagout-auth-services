package com.hangout.core.auth_api.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import com.hangout.core.auth_api.dto.request.DeviceDetails;
import com.hangout.core.auth_api.dto.response.IpDetails;
import com.hangout.core.auth_api.entity.Device;
import com.hangout.core.auth_api.entity.User;

import lombok.extern.slf4j.Slf4j;

@Testcontainers
@SpringBootTest
@ActiveProfiles("test")
@Slf4j
public class DeviceUtilTest {
        @Container
        @ServiceConnection
        static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

        @Autowired
        private DeviceUtil deviceUtil;

        @Test
        void testBuildDeviceProfile_privateIP_returnsDummyDevice() {
                DeviceDetails dummyDeviceDetails = new DeviceDetails("192.168.0.1", "test", 0, 0, "test");
                IpDetails dummyIpDetails = new IpDetails("test", Optional.empty(), "test", "test", "test", "test",
                                "test", "test", "test", false, false, false);
                User dummyUser = new User("test_user", "test_email", "test_password");
                Device dummyDevice = new Device(dummyDeviceDetails, dummyIpDetails, dummyUser);
                assertEquals(deviceUtil.buildDeviceProfile(dummyDeviceDetails, dummyUser), dummyDevice);
        }

        @Test
        void test_isNewDevice_osChange() {
                DeviceDetails oldDeviceDetails = new DeviceDetails("2401:4900:8828:9c7b:8d69:19ca:abb5:c433",
                                "ubuntu/linux", 1920, 1080, "Gecko/Chrome");
                DeviceDetails newDeviceDetails = new DeviceDetails("2401:4900:8828:9c7b:8d69:19ca:abb5:c433",
                                "Windows NT", 1920, 1080, "Gecko/Chrome");
                IpDetails ipDetails = new IpDetails("success", Optional.empty(), "Asia", "India", "Asia/Kolkata",
                                "West Bengal",
                                "Kolkata", "Bharti Airtel", "AIRTELBROADBAND-AS-AP", false, false, false);
                User user = new User("test_user", "test_email", "test_password");

                Device oldDevice = new Device(oldDeviceDetails, ipDetails, user);
                Device newDevice = new Device(newDeviceDetails, ipDetails, user);
                assertTrue(DeviceUtil.isNewDevice(oldDevice, newDevice));
        }

        @Test
        void test_isNewDevice_screenChange() {
                DeviceDetails oldDeviceDetails = new DeviceDetails("2401:4900:8828:9c7b:8d69:19ca:abb5:c433",
                                "ubuntu/linux", 1920, 1080, "Gecko/Chrome");
                DeviceDetails newDeviceDetails = new DeviceDetails("2401:4900:8828:9c7b:8d69:19ca:abb5:c433",
                                "ubuntu/linux", 1080, 720, "Gecko/Chrome");
                IpDetails ipDetails = new IpDetails("success", Optional.empty(), "Asia", "India", "Asia/Kolkata",
                                "West Bengal",
                                "Kolkata", "Bharti Airtel", "AIRTELBROADBAND-AS-AP", false, false, false);
                User user = new User("test_user", "test_email", "test_password");

                Device oldDevice = new Device(oldDeviceDetails, ipDetails, user);
                Device newDevice = new Device(newDeviceDetails, ipDetails, user);
                assertFalse(DeviceUtil.isNewDevice(oldDevice, newDevice));
        }

        @Test
        void test_isNewDevice_geoLocationPropertiesChange() {
                DeviceDetails deviceDetails = new DeviceDetails("2401:4900:8828:9c7b:8d69:19ca:abb5:c433",
                                "ubuntu/linux", 1920, 1080, "Gecko/Chrome");
                IpDetails oldIpDetails = new IpDetails("success", Optional.empty(), "Asia", "India", "Asia/Kolkata",
                                "West Bengal",
                                "Kolkata", "Bharti Airtel", "AIRTELBROADBAND-AS-AP", false, false, false);
                IpDetails newIpDetails = new IpDetails("success", Optional.empty(), "North America",
                                "United States of America", "America/New_York",
                                "Georgia",
                                "Atlanta", "Mint Mobile", "MINTBROADBAND-NA-E", false, false, false);
                User user = new User("test_user", "test_email", "test_password");

                Device oldDevice = new Device(deviceDetails, oldIpDetails, user);
                Device newDevice = new Device(deviceDetails, newIpDetails, user);
                assertTrue(DeviceUtil.isNewDevice(oldDevice, newDevice));
        }
}
