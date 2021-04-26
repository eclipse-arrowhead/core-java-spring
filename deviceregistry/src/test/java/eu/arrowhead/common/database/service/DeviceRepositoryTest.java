/********************************************************************************
 * Copyright (c) 2020 FHB
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   FHB - implementation
 *   Arrowhead Consortia - conceptualization
 ********************************************************************************/

package eu.arrowhead.common.database.service;

import java.util.List;
import java.util.Optional;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit4.SpringRunner;

import eu.arrowhead.common.database.entity.Device;
import eu.arrowhead.common.database.repository.DeviceRepository;

@RunWith(SpringRunner.class)
@DataJpaTest
@TestPropertySource(properties = {
        "spring.jpa.hibernate.ddl-auto=create"
})
@Sql(scripts = "classpath:/sql/devices.sql")
public class DeviceRepositoryTest {

    @Autowired
    private DeviceRepository deviceRepository;

    @Test
    public void injectedComponentsAreNotNull() {
        Assert.assertNotNull(deviceRepository);
    }

    @Test
    public void findByDeviceNameAndMacAddress() {
        final Optional<Device> device1 = deviceRepository.findByDeviceNameAndMacAddress("device1", "01:23:45:67:89:AB");
        Assert.assertTrue(device1.isPresent());
    }

    @Test
    public void findByDeviceNameAndMacAddress_nonExistentName() {
        final Optional<Device> device1 = deviceRepository.findByDeviceNameAndMacAddress("device", "01:23:45:67:89:AB");
        Assert.assertFalse(device1.isPresent());
    }

    @Test
    public void findByDeviceNameAndMacAddress_nonExistentMacAddress() {
        final Optional<Device> device1 = deviceRepository.findByDeviceNameAndMacAddress("device1", "FF:FF:FF:FF:FF:FF");
        Assert.assertFalse(device1.isPresent());
    }

    @Test
    public void findByDeviceName() {
        final List<Device> device2 = deviceRepository.findByDeviceName("device2");
        Assert.assertEquals(2, device2.size());

        final List<Device> device3 = deviceRepository.findByDeviceName("device3");
        Assert.assertEquals(2, device3.size());
    }

    @Test
    public void findByDeviceName_nonExistent() {
        final List<Device> devices = deviceRepository.findByDeviceName("device");
        Assert.assertEquals(0, devices.size());
    }
}