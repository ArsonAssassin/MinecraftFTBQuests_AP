# File: ftbquests_world_template.py

import random
from typing import Dict, List, Set, Any
from worlds.generic.Rules import add_rule
from BaseClasses import Region, Entrance, Location, Item, ItemClassification, Tutorial, RegionType

class FTBQuestsAP(Tutorial):
    game = "FTBQuests"
    topology_present = False
    item_name_to_id = {ITEM_NAME_TO_ID_PLACEHOLDER}
    location_name_to_id = {LOCATION_NAME_TO_ID_PLACEHOLDER}

    def __init__(self, world, player: int):
        super().__init__(world, player)

    def generate_early(self):
        self.multiworld.regions += [
            self.create_region("Menu", RegionType.Default, "Menu"),
            self.create_region("FTBQuests", RegionType.Default, "FTBQuests World")
        ]

        menu = self.multiworld.get_region("Menu", self.player)
        ftbquests = self.multiworld.get_region("FTBQuests", self.player)
        menu.connect(ftbquests, "Enter FTBQuests")

    def create_items(self):
        return [self.create_item(name) for name in self.item_name_to_id.keys()]

    def create_item(self, name: str) -> Item:
        return Item(name, ItemClassification.progression, self.item_name_to_id[name], self.player)

    def create_regions(self):
        ftbquests_region = self.multiworld.get_region("FTBQuests", self.player)
        for name, id in self.location_name_to_id.items():
            location = Location(self.player, name, id, ftbquests_region)
            ftbquests_region.locations.append(location)

    def set_rules(self):
        self.multiworld.get_entrance("Enter FTBQuests", self.player).access_rule = lambda state: True

    def generate_basic(self):
        self.items = self.create_items()
        self.create_regions()

    def fill_slot_data(self):
        return {
            "item_name_to_id": self.item_name_to_id,
            "location_name_to_id": self.location_name_to_id
        }

    @classmethod
    def stage_assert_generate(cls, world_data: Dict[str, Any]):
        assert 'item_name_to_id' in world_data, "FTBQuests world data is missing item mappings"
        assert 'location_name_to_id' in world_data, "FTBQuests world data is missing location mappings"

    def generate_output(self, output_directory: str):
        pass

    @classmethod
    def get_items_to_exclude(cls, world, player: int) -> Set[str]:
        return set()

def create_world(world, player: int) -> FTBQuestsAP:
    return FTBQuestsAP(world, player)