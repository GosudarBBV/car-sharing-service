package car.sharing.service.chs.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Filter;
import org.hibernate.annotations.FilterDef;
import org.hibernate.annotations.ParamDef;
import org.hibernate.annotations.SQLDelete;

@Entity
@Table(
        name = "cars",
        indexes = {
                @Index(name = "idx_cars_brand_type", columnList = "brand,type")
        }
)
@SQLDelete(sql = "UPDATE cars SET is_deleted = true WHERE id = ?")
@FilterDef(name = "carDeletedFilter",
        parameters = @ParamDef(name = "isDeleted", type = boolean.class))
@Filter(name = "carDeletedFilter",
        condition = "is_deleted = :isDeleted")
@Getter
@Setter
public class Car implements SoftDeletable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String brand;

    @Column(nullable = false)
    private String model;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CarType type;

    @Column(nullable = false)
    private int inventory;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal dailyFee;

    @Column(nullable = false)
    private boolean isDeleted = false;
}
