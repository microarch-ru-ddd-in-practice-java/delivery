package model;

import java.net.URI;
import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import org.openapitools.jackson.nullable.JsonNullable;
import java.time.OffsetDateTime;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import io.swagger.v3.oas.annotations.media.Schema;


import java.util.*;
import jakarta.annotation.Generated;

/**
 * Address
 */

@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", date = "2026-07-05T14:03:31.806648+03:00[Europe/Moscow]")
public class Address {

  private String country;

  private String city;

  private String street;

  private String house;

  private String apartment;

  public Address() {
    super();
  }

  /**
   * Constructor with only required parameters
   */
  public Address(String country, String city, String street, String house, String apartment) {
    this.country = country;
    this.city = city;
    this.street = street;
    this.house = house;
    this.apartment = apartment;
  }

  public Address country(String country) {
    this.country = country;
    return this;
  }

  /**
   * Страна
   * @return country
  */
  @NotNull 
  @Schema(name = "country", description = "Страна", requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonProperty("country")
  public String getCountry() {
    return country;
  }

  public void setCountry(String country) {
    this.country = country;
  }

  public Address city(String city) {
    this.city = city;
    return this;
  }

  /**
   * Город
   * @return city
  */
  @NotNull 
  @Schema(name = "city", description = "Город", requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonProperty("city")
  public String getCity() {
    return city;
  }

  public void setCity(String city) {
    this.city = city;
  }

  public Address street(String street) {
    this.street = street;
    return this;
  }

  /**
   * Улица
   * @return street
  */
  @NotNull 
  @Schema(name = "street", description = "Улица", requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonProperty("street")
  public String getStreet() {
    return street;
  }

  public void setStreet(String street) {
    this.street = street;
  }

  public Address house(String house) {
    this.house = house;
    return this;
  }

  /**
   * Дом
   * @return house
  */
  @NotNull 
  @Schema(name = "house", description = "Дом", requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonProperty("house")
  public String getHouse() {
    return house;
  }

  public void setHouse(String house) {
    this.house = house;
  }

  public Address apartment(String apartment) {
    this.apartment = apartment;
    return this;
  }

  /**
   * Квартира
   * @return apartment
  */
  @NotNull 
  @Schema(name = "apartment", description = "Квартира", requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonProperty("apartment")
  public String getApartment() {
    return apartment;
  }

  public void setApartment(String apartment) {
    this.apartment = apartment;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Address address = (Address) o;
    return Objects.equals(this.country, address.country) &&
        Objects.equals(this.city, address.city) &&
        Objects.equals(this.street, address.street) &&
        Objects.equals(this.house, address.house) &&
        Objects.equals(this.apartment, address.apartment);
  }

  @Override
  public int hashCode() {
    return Objects.hash(country, city, street, house, apartment);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class Address {\n");
    sb.append("    country: ").append(toIndentedString(country)).append("\n");
    sb.append("    city: ").append(toIndentedString(city)).append("\n");
    sb.append("    street: ").append(toIndentedString(street)).append("\n");
    sb.append("    house: ").append(toIndentedString(house)).append("\n");
    sb.append("    apartment: ").append(toIndentedString(apartment)).append("\n");
    sb.append("}");
    return sb.toString();
  }

  /**
   * Convert the given object to string with each line indented by 4 spaces
   * (except the first line).
   */
  private String toIndentedString(Object o) {
    if (o == null) {
      return "null";
    }
    return o.toString().replace("\n", "\n    ");
  }
}

