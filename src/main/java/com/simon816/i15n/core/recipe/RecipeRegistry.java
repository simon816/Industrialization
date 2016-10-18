package com.simon816.i15n.core.recipe;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.recipe.ShapedRecipe;

import com.flowpowered.math.vector.Vector2i;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.simon816.i15n.core.ImplUtil;
import com.simon816.i15n.core.item.CustomItem;

public class RecipeRegistry {


    public static class RecipeBuilder {

        private final ItemStack outputItem;
        private final List<char[]> rowData = Lists.newArrayList();
        private final Map<Character, ItemStack> replacements = Maps.newHashMap();
        private int width;

        public RecipeBuilder(CustomItem item) {
            this.outputItem = item.createItemStack();
        }

        public RecipeBuilder row(String elements) {
            return row(elements.toCharArray());
        }

        public RecipeBuilder row(char... elements) {
            this.width = Math.max(this.width, elements.length);
            this.rowData.add(elements);
            return this;
        }

        public RecipeBuilder replace(char elem, ItemType item) {
            this.replacements.put(elem, ItemStack.of(item, 1));
            return this;
        }

        public void register() {
            ShapedRecipe.Builder builder;
            try {
                builder =
                        Sponge.getRegistry().createBuilder(ShapedRecipe.Builder.class);
            } catch (IllegalArgumentException e) {
                Object[] params = new Object[this.rowData.size() + (2 * this.replacements.size())];
                int i = 0;
                for (char[] row : this.rowData) {
                    params[i++] = new String(row);
                }
                for (Entry<Character, ItemStack> entry: this.replacements.entrySet()) {
                    params[i++] = entry.getKey();
                    params[i++] = entry.getValue();
                }
                ImplUtil.registerShapedRecipe(this.outputItem, params);
                return;
            }
            builder.dimensions(new Vector2i(this.width, this.rowData.size()))
                    .addResult(this.outputItem);
            for (int i = 0; i < this.rowData.size(); i++) {
                char[] row = this.rowData.get(i);
                ItemStack[] rowItems = new ItemStack[row.length];
                for (int j = 0; j < row.length; j++) {
                    rowItems[j] = this.replacements.get(row[j]);
                }
                builder.row(i, rowItems);
            }
            ShapedRecipe recipe = builder.build();
            Sponge.getRegistry().getRecipeRegistry().register(recipe);
        }

    }

    public static RecipeBuilder builder(CustomItem item) {
        return new RecipeBuilder(item);
    }

}
