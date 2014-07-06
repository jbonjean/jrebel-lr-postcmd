/*
 * Copyright (C) 2014 Julien Bonjean <julien@bonjean.info>
 * 
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 3 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 51
 * Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 */
package info.bonjean.jrebel.liferay.postcmd;

import org.zeroturnaround.javarebel.ClassResourceSource;
import org.zeroturnaround.javarebel.Integration;
import org.zeroturnaround.javarebel.IntegrationFactory;
import org.zeroturnaround.javarebel.Plugin;

public class PostCmdPlugin implements Plugin
{
	public void preinit()
	{
		ClassLoader classLoader = getClass().getClassLoader();
		Integration integration = IntegrationFactory.getInstance();
		integration.addIntegrationProcessor(classLoader, "com.liferay.portal.deploy.hot.PortletHotDeployListener",
				new HotDeployListenerCBP());
	}

	public boolean checkDependencies(ClassLoader classLoader, ClassResourceSource classResourceSource)
	{
		return classResourceSource.getClassResource("com.liferay.portal.deploy.hot.PortletHotDeployListener") != null;
	}

	public String getId()
	{
		return "lr_postcmd_plugin";
	}

	public String getName()
	{
		return "Liferay post-cmd plugin";
	}

	public String getDescription()
	{
		return "<li>Execute a system command after a class or resource reload</li>";
	}

	public String getAuthor()
	{
		return null;
	}

	public String getWebsite()
	{
		return null;
	}

	public String getSupportedVersions()
	{
		return "Liferay 6";
	}

	public String getTestedVersions()
	{
		return "Liferay 6";
	}

}
