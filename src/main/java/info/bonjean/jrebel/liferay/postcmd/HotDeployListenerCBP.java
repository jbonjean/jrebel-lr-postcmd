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

import org.zeroturnaround.bundled.javassist.ClassPool;
import org.zeroturnaround.bundled.javassist.CtClass;
import org.zeroturnaround.javarebel.integration.support.JavassistClassBytecodeProcessor;

/**
 * 
 * @author Julien Bonjean <julien@bonjean.info>
 * 
 */
public class HotDeployListenerCBP extends JavassistClassBytecodeProcessor
{
	public void process(ClassPool classPool, ClassLoader classLoader, CtClass ctClass) throws Exception
	{
		classPool.importPackage("org.zeroturnaround.javarebel");
		classPool.importPackage("info.bonjean.jrebel.liferay.postcmd");

		ctClass.getDeclaredMethod("invokeDeploy")
				.insertAfter(
						"{ if (hotDeployEvent.getServletContext() instanceof RebelServletContext"
								+ " && ServletIntegrationFactory.getInstance().hasReplacedResources((RebelServletContext)hotDeployEvent.getServletContext()) ) {"
								+ " ResourceReloader.getInstance().register(hotDeployEvent); }}");
	}
}
