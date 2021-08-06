package com.wilterson.javersdemo.web;

import lombok.*;

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UpdatePriceDto {

    private double price;
}
