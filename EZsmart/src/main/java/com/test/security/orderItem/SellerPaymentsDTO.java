package com.test.security.orderItem;

import com.test.security.user.User;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SellerPaymentsDTO {

    private Double paymentAmount;
    private LocalDateTime paymentDate;
    private User user;
}
