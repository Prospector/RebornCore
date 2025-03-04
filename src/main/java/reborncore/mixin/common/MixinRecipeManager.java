package reborncore.mixin.common;

import net.minecraft.inventory.Inventory;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeManager;
import net.minecraft.recipe.RecipeType;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import reborncore.mixin.extensions.RecipeManagerExtensions;

import java.util.Map;

@Mixin(RecipeManager.class)
public abstract class MixinRecipeManager implements RecipeManagerExtensions {

	@Shadow protected abstract <C extends Inventory, T extends Recipe<C>> Map<Identifier, Recipe<C>> getAllOfType(RecipeType<T> recipeType_1);

	@Override
	public <C extends Inventory, T extends Recipe<C>> Map<Identifier, Recipe<C>> getAll(RecipeType<T> type) {
		return getAllOfType(type);
	}


}
