# AH-Ditto Adapter
 
This adapter enables the integration of Eclipse Ditto and Eclipse Arrowhead. The objective is to support virtual representations to front physical devices and provide their services. We use the term digital twin as a proxy (DTaaP), or shortly digital proxy. Benefits of having a digital proxy that provide services on behalf of a device include:
+ Energy Efficiency: devices can sleep (duty cycle) while digital proxy services are available.
+ Availability & Persistence: Digital proxy provides seamless persistency to service consumers. Physical device can be replaced.

+ Security: Devices not capable of strong security interacts only with their digital proxy, while the digital proxy registers services and provides strong security to consumers

Please see the [AH-Ditto Digital Twin - System of systems Description (SosD)] and [AH-Ditto Digital Twin - System Description (SysD)] for further details.

## Services

AH-Ditto provides two different services:
 + [Management services] - These services are responsible to do administrative operations related to the Things. These management operations include CRUD operations related to policies, Things, DTs (Digital Twin), connections, and their services. These services have all access rights to do all operations. Only admin users will be allowed to consume management services. 
 + [Application services] - These services are for consumer applications systems. Any consumer from Eclipse Arrowhead meeting the security criteria of the framework can access these services via AH-Ditto. With these services consumers will be able to read the status of desired DTs (Digital Twins) from Eclipse Ditto through AH-Ditto. Consumer services registered within the Eclipse Arrowhead framework will provide “READ” access. 
 

 [AH-Ditto Digital Twin - System of systems Description (SosD)]:documentation/ah-ditto/AH-DittoAdapterSystem-SystemofSystemsDescription(SosD).md

 [AH-Ditto Digital Twin - System Description (SysD)]:documentation/ah-ditto/AH-Ditto-Adapter-System:System-Description-(SysD).md

 [Management services]:documentation/ah-ditto/AH-DittoManagement-InterfaceDesignDescription-(IDD).md

 [Application services]:documentation/ah-ditto/AH-DittoApplication-InterfaceDesignDescription-(IDD).md