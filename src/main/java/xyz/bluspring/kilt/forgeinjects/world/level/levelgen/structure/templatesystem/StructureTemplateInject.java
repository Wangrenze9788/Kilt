package xyz.bluspring.kilt.forgeinjects.world.level.levelgen.structure.templatesystem;

import com.google.common.collect.Lists;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import xyz.bluspring.kilt.helpers.mixin.CreateStatic;
import xyz.bluspring.kilt.injections.world.level.levelgen.structure.templatesystem.StructureProcessorInjection;
import xyz.bluspring.kilt.injections.world.level.levelgen.structure.templatesystem.StructureTemplateInjection;

import javax.annotation.Nullable;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

@Mixin(StructureTemplate.class)
public abstract class StructureTemplateInject implements StructureTemplateInjection {
    private static final AtomicReference<StructureTemplate> kilt$template = new AtomicReference<>(null);

    @Shadow
    public static Vec3 transform(Vec3 target, Mirror mirror, Rotation rotation, BlockPos centerOffset) {
        return null;
    }

    @CreateStatic
    private static List<StructureTemplate.StructureBlockInfo> processBlockInfos(LevelAccessor level, BlockPos pos, BlockPos pos2, StructurePlaceSettings structurePlaceSettings, List<StructureTemplate.StructureBlockInfo> list, @Nullable StructureTemplate template) {
        kilt$template.set(template);
        return StructureTemplate.processBlockInfos(level, pos, pos2, structurePlaceSettings, list);
    }

    @Redirect(method = "processBlockInfos(Lnet/minecraft/world/level/LevelAccessor;Lnet/minecraft/core/BlockPos;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/levelgen/structure/templatesystem/StructurePlaceSettings;Ljava/util/List;)Ljava/util/List;", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/levelgen/structure/templatesystem/StructureProcessor;processBlock(Lnet/minecraft/world/level/LevelReader;Lnet/minecraft/core/BlockPos;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/levelgen/structure/templatesystem/StructureTemplate$StructureBlockInfo;Lnet/minecraft/world/level/levelgen/structure/templatesystem/StructureTemplate$StructureBlockInfo;Lnet/minecraft/world/level/levelgen/structure/templatesystem/StructurePlaceSettings;)Lnet/minecraft/world/level/levelgen/structure/templatesystem/StructureTemplate$StructureBlockInfo;"))
    private static StructureTemplate.StructureBlockInfo kilt$useForgeProcess(StructureProcessor instance, LevelReader levelReader, BlockPos blockPos, BlockPos blockPos2, StructureTemplate.StructureBlockInfo structureBlockInfo, StructureTemplate.StructureBlockInfo structureBlockInfo2, StructurePlaceSettings structurePlaceSettings) {
        if (kilt$template.get() != null)
            return ((StructureProcessorInjection) instance).process(levelReader, blockPos, blockPos2, structureBlockInfo, structureBlockInfo2, structurePlaceSettings, kilt$template.getAndSet(null));
        else
            return instance.processBlock(levelReader, blockPos, blockPos2, structureBlockInfo, structureBlockInfo2, structurePlaceSettings);
    }
}
