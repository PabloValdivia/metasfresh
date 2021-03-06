package de.metas.fresh.picking;

/*
 * #%L
 * de.metas.fresh.base
 * %%
 * Copyright (C) 2015 metas GmbH
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 2 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-2.0.html>.
 * #L%
 */


import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.adempiere.util.Services;
import org.adempiere.util.TypedAccessor;
import org.adempiere.util.comparator.AccessorComparator;
import org.adempiere.util.comparator.ComparableComparator;

import de.metas.adempiere.form.terminal.context.ITerminalContext;
import de.metas.picking.api.IPickingSlotDAO;
import de.metas.picking.model.I_M_PickingSlot;

public class PickingSlotKeyBuilder
{
	private final ITerminalContext terminalContext;

	private final Map<Integer, PickingSlotKey> pickingSlotsKeys = new HashMap<Integer, PickingSlotKey>();

	/**
	 * Sort by PickingSlot string
	 */
	private static final Comparator<PickingSlotKey> pickingSlotKeysComparator = new AccessorComparator<PickingSlotKey, String>(
			ComparableComparator.<String>getInstance(),
			new TypedAccessor<String>()
			{

				@Override
				public String getValue(Object o)
				{
					if (o == null)
					{
						return "";
					}
					final PickingSlotKey pickingSlotKey = (PickingSlotKey)o;
					return pickingSlotKey.getM_PickingSlot().getPickingSlot();
				}
			});


	public PickingSlotKeyBuilder(final ITerminalContext terminalContext)
	{
		super();
		this.terminalContext = terminalContext;
	}

	public void addBPartner(final int bpartnerId, final int bpartnerLocationId, final Set<Integer> allowedWarehouseIds)
	{
		final Properties ctx = terminalContext.getCtx();
		final List<I_M_PickingSlot> bpPickingSlots = Services.get(IPickingSlotDAO.class).retrivePickingSlotsForBPartner(ctx, bpartnerId, bpartnerLocationId);
		for (final I_M_PickingSlot pickingSlot : bpPickingSlots)
		{
			if (!pickingSlot.isActive())
			{
				continue;
			}

			final int pickingSlotId = pickingSlot.getM_PickingSlot_ID();
			if (pickingSlotsKeys.containsKey(pickingSlotId))
			{
				// already added
				continue;
			}
			
			//
			// Filter by warehouse
			if (allowedWarehouseIds != null && !allowedWarehouseIds.isEmpty())
			{
				final int warehouseId = pickingSlot.getM_Warehouse_ID();
				if (!allowedWarehouseIds.contains(warehouseId))
				{
					// skip because it's not in our list of accepted warehouses
					continue;
				}
			}

			final PickingSlotKey pickingSlotKey = new PickingSlotKey(terminalContext, pickingSlot);
			pickingSlotsKeys.put(pickingSlotId, pickingSlotKey);
		}
	}
	

	public List<PickingSlotKey> getPickingSlotKeys()
	{
		if (pickingSlotsKeys.isEmpty())
		{
			return Collections.emptyList();
		}

		final List<PickingSlotKey> result = new ArrayList<PickingSlotKey>(pickingSlotsKeys.values());
		Collections.sort(result, pickingSlotKeysComparator);
		
		return result;
	}
}
