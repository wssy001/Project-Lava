package moe.ofs.backend.domain.dcs.poll;

import com.google.gson.annotations.SerializedName;
import lombok.*;
import moe.ofs.backend.domain.connector.Level;
import moe.ofs.backend.domain.dcs.BaseEntity;
import moe.ofs.backend.domain.dcs.LuaState;
import moe.ofs.backend.domain.dcs.theater.Vector3D;
import moe.ofs.backend.domain.dcs.theater.GeoPosition;

import javax.persistence.*;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Setter
@Getter
@NoArgsConstructor
@LuaState(Level.EXPORT_POLL)
@Entity
@ToString
@Table(name = "export_object")
public final class ExportObject extends BaseEntity implements Serializable {
    @Column(name = "own_bank")
    @SerializedName("Bank")
    private Double bank;

    @Column(name = "coalition")
    @SerializedName("Coalition")
    private String coalition;

    @Column(name = "coalition_id")
    @SerializedName("CoalitionID")
    private int coalitionID;

    @Column(name = "country_id")
    @SerializedName("Country")
    private int country;

    @Column(name = "group_name")
    @SerializedName("GroupName")
    private String groupName;

    @Column(name = "own_heading")
    @SerializedName("Heading")
    private Double heading;

    @Column(name = "own_name")
    @SerializedName("Name")
    private String name;

    @Column(name = "own_pitch")
    @SerializedName("Pitch")
    private Double pitch;

    @Column(name = "runtime_id")
    @SerializedName("RuntimeID")
    private long runtimeID;

    @Column(name = "unit_name")
    @SerializedName("UnitName")
    private String unitName;

    @ElementCollection(fetch = FetchType.EAGER)
    @MapKeyColumn(name = "status_name")
    @Column(name = "is_active")
    @CollectionTable(name = "object_status", joinColumns = @JoinColumn(name = "id"))
    @SerializedName("Flags")
    private Map<String, Boolean> status;

//    @ElementCollection(fetch = FetchType.EAGER)
//    @MapKeyColumn(name="key")
//    @Column(name="value")
//    @CollectionTable(name="lat_lon_alt", joinColumns=@JoinColumn(name="id"))
//    @SerializedName("LatLongAlt")
//    private Map<String, Double> latLongAlt;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "geoposition_id", referencedColumnName = "id")
    @SerializedName("LatLongAlt")
    private GeoPosition geoPosition;

//    @ElementCollection(fetch = FetchType.EAGER)
//    @MapKeyColumn(name="key")
//    @Column(name="value")
//    @CollectionTable(name="position", joinColumns=@JoinColumn(name="id"))
//    @SerializedName("Position")
//    private Map<String, Double> position;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "vector3d_id", referencedColumnName = "id")
    @SerializedName("Position")
    private Vector3D position;

    @ElementCollection(fetch = FetchType.EAGER)
    @MapKeyColumn(name = "attribute_key")
    @Column(name = "attribute_value")
    @CollectionTable(name = "object_attribute", joinColumns = @JoinColumn(name = "id"))
    @SerializedName("Type")
    private Map<String, Integer> type;

    @Builder
    public ExportObject(Long id, double bank, String coalition, int coalitionID, int country,
                        String groupName, double heading,
                        String name, double pitch, int runtimeID, String unitName,
                        Map<String, Boolean> status, GeoPosition geoPosition,
                        Vector3D position, Map<String, Integer> type) {
        super(id);
        this.bank = bank;
        this.coalition = coalition;
        this.coalitionID = coalitionID;
        this.country = country;
        this.groupName = groupName;
        this.heading = heading;
        this.name = name;
        this.pitch = pitch;
        this.runtimeID = runtimeID;
        this.unitName = unitName;

        this.status = status;
        this.geoPosition = geoPosition;
        this.position = position;
        this.type = type;
    }

    public ExportObject(ExportObject object) {
        super(object.getId());
        this.bank = object.getBank();
        this.coalition = object.getCoalition();
        this.coalitionID = object.getCoalitionID();
        this.country = object.getCountry();
        this.groupName = object.getGroupName();
        this.heading = object.getHeading();
        this.name = object.getName();
        this.pitch = object.getPitch();
        this.runtimeID = object.getRuntimeID();
        this.unitName = object.getUnitName();

        this.status = new HashMap<>(object.getStatus());
        this.geoPosition = object.getGeoPosition();
        this.position = object.getPosition();
        this.type = new HashMap<>(object.getType());
    }

    /**
     * Two ExportObject is consider equal if runtime id and unitName is the same.
     *
     * @param o the object to be tested equality with.
     * @return boolean value indicating whether two ExportObject are equal.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ExportObject that = (ExportObject) o;

        if (runtimeID != that.runtimeID) return false;
        return Objects.equals(unitName, that.unitName);
    }

    @Override
    public int hashCode() {
        int result = (int) (runtimeID ^ (runtimeID >>> 32));
        result = 31 * result + (unitName != null ? unitName.hashCode() : 0);
        return result;
    }

    public GeoPosition getGeoPosition() {
        return geoPosition;
    }


    public Vector3D getPosition() {
        return position;
    }
}
