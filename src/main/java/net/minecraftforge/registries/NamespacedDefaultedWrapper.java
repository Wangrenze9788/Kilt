/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.minecraftforge.registries;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Lifecycle;
import net.minecraft.core.DefaultedRegistry;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
import org.apache.commons.lang3.Validate;
import org.jetbrains.annotations.Nullable;
import xyz.bluspring.kilt.injections.core.MappedRegistryInjection;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Stream;

class NamespacedDefaultedWrapper<T> extends DefaultedRegistry<T> implements ILockableRegistry, IHolderHelperHolder<T>
{
    private final ForgeRegistry<T> delegate;
    private final NamespacedHolderHelper<T> holders;

    private boolean locked = false;
    private Lifecycle elementsLifecycle = Lifecycle.experimental();

    private NamespacedDefaultedWrapper(ForgeRegistry<T> owner, Function<T, Holder.Reference<T>> holderLookup)
    {
        super("empty", owner.getRegistryKey(), Lifecycle.experimental(), holderLookup);
        this.delegate = owner;
        this.holders = new NamespacedHolderHelper<>(owner, this, this.delegate.getDefaultKey(), holderLookup);
    }

    @Override
    public Holder<T> registerMapping(final int id, final ResourceKey<T> key, final T value, final Lifecycle lifecycle)
    {
        if (locked)
            throw new IllegalStateException("Can not register to a locked registry. Modder should use Forge Register methods.");

        Validate.notNull(value);
        MappedRegistryInjection.getKnownRegistries().add(this.key().location());
        this.elementsLifecycle = this.elementsLifecycle.add(lifecycle);

        T oldValue = this.delegate.getRaw(key.location());

        int realId = this.delegate.add(id, key.location(), value);

        return this.holders.onAdded(RegistryManager.ACTIVE, realId, key, value, oldValue);
    }

    @Override
    public Holder<T> register(final ResourceKey<T> p_205891_, final T p_205892_, final Lifecycle p_205893_)
    {
        return registerMapping(-1, p_205891_, p_205892_, p_205893_);
    }

    @Override
    public Holder<T> registerOrOverride(final OptionalInt id, final ResourceKey<T> p_205885_, final T p_205886_, final Lifecycle p_205887_)
    {
        int wanted = -1;
        if (id.isPresent() && byId(id.getAsInt()) != null)
            wanted = id.getAsInt();
        return registerMapping(wanted, p_205885_, p_205886_, p_205887_);
    }

    // Reading Functions
    @Override
    public Optional<T> getOptional(@Nullable ResourceLocation name)
    {
        return Optional.ofNullable( this.delegate.getRaw(name)); //get without default
    }

    @Override
    @Nullable
    public T get(@Nullable ResourceLocation name)
    {
        return this.delegate.getValue(name); //getOrDefault
    }

    @Override
    @Nullable
    public T get(@Nullable ResourceKey<T> name)
    {
        return name == null ? null : this.delegate.getRaw(name.location()); //get without default
    }

    @Override
    @Nullable
    public ResourceLocation getKey(T value)
    {
        return this.delegate.getKey(value);
    }

    @Override
    public Optional<ResourceKey<T>> getResourceKey(T p_122755_)
    {
        return this.delegate.getResourceKey(p_122755_);
    }

    @Override
    public boolean containsKey(ResourceLocation key)
    {
        return this.delegate.containsKey(key);
    }

    @Override
    public boolean containsKey(ResourceKey<T> key)
    {
       return this.delegate.getRegistryName().equals(key.registry()) && containsKey(key.location());
    }

    @Override
    public int getId(@Nullable T value)
    {
        return this.delegate.getID(value);
    }

    @Override
    @Nullable
    public T byId(int id)
    {
        return this.delegate.getValue(id);
    }

    @Override
    public ResourceLocation getDefaultKey()
    {
        return this.delegate.getDefaultKey();
    }

    @Override
    public Lifecycle lifecycle(T value)
    {
        return Lifecycle.stable();
    }

    @Override
    public Lifecycle elementsLifecycle()
    {
       return this.elementsLifecycle;
    }

    @Override
    public Iterator<T> iterator()
    {
        return this.delegate.iterator();
    }

    @Override
    public Set<ResourceLocation> keySet()
    {
        return this.delegate.getKeys();
    }

    @Override
    public Set<ResourceKey<T>> registryKeySet()
    {
        return this.delegate.getResourceKeys();
    }

    @Override
    public Set<Map.Entry<ResourceKey<T>, T>> entrySet()
    {
        return this.delegate.getEntries();
    }

    @Override
    public boolean isEmpty()
    {
        return this.delegate.isEmpty();
    }

    @Override
    public int size()
    {
        return this.delegate.size();
    }

    @Override
    public NamespacedHolderHelper<T> getHolderHelper()
    {
        return this.holders;
    }

    @Override public Optional<Holder<T>> getHolder(int id) { return this.holders.getHolder(id); }
    @Override public Optional<Holder<T>> getHolder(ResourceKey<T> key) { return this.holders.getHolder(key); }
    @Override public DataResult<Holder<T>> getOrCreateHolder(ResourceKey<T> key) { return this.holders.getOrCreateHolder(key); }
    @Override public Holder<T> getOrCreateHolderOrThrow(ResourceKey<T> key) { return this.holders.getOrCreateHolderOrThrow(key); }
    @Override public Optional<Holder<T>> getRandom(RandomSource rand) { return this.holders.getRandom(rand); }
    @Override public Stream<Holder.Reference<T>> holders() { return this.holders.holders();  }
    @Override public boolean isKnownTagName(TagKey<T> name) { return this.holders.isKnownTagName(name); }
    @Override public Stream<Pair<TagKey<T>, HolderSet.Named<T>>> getTags() { return this.holders.getTags(); }
    @Override public HolderSet.Named<T> getOrCreateTag(TagKey<T> name) { return this.holders.getOrCreateTag(name); }
    @Override public Stream<TagKey<T>> getTagNames() { return this.holders.getTagNames(); }
    @Override public Registry<T> freeze() { return this.holders.freeze(); }
    @Override public Holder.Reference<T> createIntrusiveHolder(T value) { return this.holders.createIntrusiveHolder(value); }
    @Override public Optional<HolderSet.Named<T>> getTag(TagKey<T> name) { return this.holders.getTag(name); }
    @Override public void bindTags(Map<TagKey<T>, List<Holder<T>>> newTags) { this.holders.bindTags(newTags); }
    @Override public void resetTags() { this.holders.resetTags(); }
    //@Deprecated @Override public void unfreeze() { this.holders.unfreeze(); }

    /** @deprecated Forge: For internal use only. Use the Register events when registering values. */
    @Deprecated @Override public void lock(){ this.locked = true; }


    public static class Factory<V> implements IForgeRegistry.CreateCallback<V>, IForgeRegistry.AddCallback<V>
    {
        public static final ResourceLocation ID = new ResourceLocation("forge", "registry_defaulted_wrapper");

        @Override
        public void onCreate(IForgeRegistryInternal<V> owner, RegistryManager stage)
        {
            ForgeRegistry<V> fowner = (ForgeRegistry<V>)owner;
            owner.setSlaveMap(ID, new NamespacedDefaultedWrapper<V>(fowner, fowner.getBuilder().getVanillaHolder()));
        }

        @Override
        @SuppressWarnings("unchecked")
        public void onAdd(IForgeRegistryInternal<V> owner, RegistryManager stage, int id, ResourceKey<V> key, V value, V oldValue)
        {
            owner.getSlaveMap(ID, NamespacedDefaultedWrapper.class).holders.onAdded(stage, id, key, value, oldValue);
        }
    }
}
