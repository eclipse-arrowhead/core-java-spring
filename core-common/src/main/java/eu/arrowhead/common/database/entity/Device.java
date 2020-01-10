package eu.arrowhead.common.database.entity;

import eu.arrowhead.common.CoreDefaults;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import javax.persistence.*;
import javax.validation.constraints.Size;
import java.time.ZonedDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "device", uniqueConstraints = @UniqueConstraint(columnNames = { "deviceName", "macAddress" }))
@NamedEntityGraph(name = "deviceWithSystemRegistryEntries",
				  attributeNodes = {
						  @NamedAttributeNode(value = "systemRegistryEntries")
})
public class Device
{

	//=================================================================================================
	// members

	public static final List<String> SORTABLE_FIELDS_BY = List.of("id", "updatedAt", "createdAt", "deviceName", "address"); //NOSONAR

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private long id;

	@Column(nullable = false, length = CoreDefaults.VARCHAR_BASIC)
	private String deviceName;

	@Column(nullable = false, length = CoreDefaults.VARCHAR_BASIC)
	private String address;

	@Column(nullable = false, length = CoreDefaults.VARCHAR_BASIC)
	private String macAddress;

	@Column(nullable = false, length = CoreDefaults.VARCHAR_EXTENDED)
	private String authenticationInfo;

	@Column(nullable = false, updatable = false, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
	private ZonedDateTime createdAt;

	@Column(nullable = false, updatable = false, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP")
	private ZonedDateTime updatedAt;

	@OneToMany(mappedBy = "device", fetch = FetchType.LAZY, orphanRemoval = true)
	@OnDelete(action = OnDeleteAction.CASCADE)
	private Set<SystemRegistry> systemRegistryEntries = new HashSet<>();


	//=================================================================================================
	// methods

	//-------------------------------------------------------------------------------------------------
	public Device() {}

	//-------------------------------------------------------------------------------------------------
	public Device(final String deviceName, final String address, final String macAddress, final String authenticationInfo) {
		this.deviceName = deviceName;
		this.address = address;
		this.macAddress = macAddress;
		this.authenticationInfo = authenticationInfo;
	}

	//-------------------------------------------------------------------------------------------------
	@PrePersist
	public void onCreate() {
		this.createdAt = ZonedDateTime.now();
		this.updatedAt = this.createdAt;
	}

	//-------------------------------------------------------------------------------------------------
	@PreUpdate
	public void onUpdate() {
		this.updatedAt = ZonedDateTime.now();
	}

	//-------------------------------------------------------------------------------------------------
	public long getId() { return id; }
	public String getDeviceName() { return deviceName; }
	public String getAddress() { return address; }
	public String getMacAddress() {return macAddress; }

	public String getAuthenticationInfo() { return authenticationInfo; }
	public ZonedDateTime getCreatedAt() { return createdAt; }
	public ZonedDateTime getUpdatedAt() { return updatedAt; }
	public Set<SystemRegistry> getSystemRegistryEntries() { return systemRegistryEntries; }

	//-------------------------------------------------------------------------------------------------
	public void setId(final long id) { this.id = id; }
	public void setDeviceName(final String systemName) { this.deviceName = systemName; }
	public void setAddress(final String address) { this.address = address; }
	public void setMacAddress(final String macAddress) { this.macAddress = macAddress; }
	public void setAuthenticationInfo(final String authenticationInfo) { this.authenticationInfo = authenticationInfo; }
	public void setCreatedAt(final ZonedDateTime createdAt) { this.createdAt = createdAt; }
	public void setUpdatedAt(final ZonedDateTime updatedAt) { this.updatedAt = updatedAt; }
	public void setSystemRegistryEntries(final Set<SystemRegistry> systemRegistryEntries) { this.systemRegistryEntries = systemRegistryEntries; }

	//-------------------------------------------------------------------------------------------------
	@Override
	public String toString() {
		return "System [id = " + id + ", systemName = " + deviceName + ", address = " + address + ", macAddress = " + macAddress + "]";
	}
}