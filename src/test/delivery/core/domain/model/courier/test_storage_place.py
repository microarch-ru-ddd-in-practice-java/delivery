from uuid import uuid4

import pytest

from delivery.core.domain.model.courier.storage_place import (
    StoragePlace,
    InvalidTotalVolume,
    InvalidName,
    StorageError,
    InvalidOrderId,
)


@pytest.fixture
def valid_storage_place():
    return StoragePlace(name="bag", total_volume=20.1)


def test_creation():
    _ = StoragePlace(name="bag", total_volume=20.1)


def test_total_volume_update_valid(valid_storage_place):
    valid_storage_place.total_volume = 1


@pytest.mark.parametrize(
    "bad_value",
    [0, "bad_value"],
)
def test_invalid_volume_creation(bad_value):
    with pytest.raises(InvalidTotalVolume):
        StoragePlace(
            name="bag",
            total_volume=bad_value,
        )


@pytest.mark.parametrize(
    "bad_value",
    [0, "bad_value"],
)
def test_invalid_volume_update(bad_value, valid_storage_place):
    with pytest.raises(InvalidTotalVolume):
        valid_storage_place.total_volume = bad_value


def test_name_update_valid(valid_storage_place):
    valid_storage_place.name = "backpack"


@pytest.mark.parametrize(
    "bad_value",
    [0, ""],
)
def test_invalid_name_creation(bad_value):
    with pytest.raises(InvalidName):
        StoragePlace(
            name=bad_value,
            total_volume=13,
        )


@pytest.mark.parametrize(
    "bad_value",
    [0, ""],
)
def test_invalid_name_update(bad_value, valid_storage_place):
    with pytest.raises(InvalidName):
        valid_storage_place.name = bad_value


def test_actually_can_fit(valid_storage_place):
    assert valid_storage_place.can_fit(5)


@pytest.mark.parametrize(
    "order_id, expected",
    [(uuid4(), True), (None, False)],
)
def test_is_empty(order_id, expected):
    storage_place = StoragePlace(name="bag", total_volume=20.1, order_id=order_id)
    assert storage_place._is_occupied() == expected


def test_put_valid_order(valid_storage_place):
    valid_storage_place.put_order(uuid4(), 5)


def test_put_exact_size_order(valid_storage_place):
    valid_storage_place.put_order(uuid4(), valid_storage_place.total_volume)


def test_put_too_large_order(valid_storage_place):
    with pytest.raises(StorageError):
        valid_storage_place.put_order(uuid4(), 100500)


def test_put_bad_id_order(valid_storage_place):
    with pytest.raises(InvalidOrderId):
        valid_storage_place.put_order(3213, 10)


def test_put_second_order(valid_storage_place):
    valid_storage_place.put_order(uuid4(), 10)
    with pytest.raises(StorageError):
        valid_storage_place.put_order(uuid4(), 100500)


def test_extract_order(valid_storage_place):
    valid_storage_place.put_order(uuid4(), 10)
    valid_storage_place.extract_order()
    assert valid_storage_place.order_id is None


def test_extract_with_no_order(valid_storage_place):
    with pytest.raises(StorageError):
        valid_storage_place.extract_order()
