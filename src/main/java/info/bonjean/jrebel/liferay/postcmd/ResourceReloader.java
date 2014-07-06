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

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import javax.servlet.ServletContext;

import org.apache.catalina.loader.WebappClassLoader;
import org.zeroturnaround.javarebel.ClassEventListener;
import org.zeroturnaround.javarebel.FileEventListener;
import org.zeroturnaround.javarebel.Logger;
import org.zeroturnaround.javarebel.LoggerFactory;
import org.zeroturnaround.javarebel.RebelServletContext;
import org.zeroturnaround.javarebel.RebelSource;
import org.zeroturnaround.javarebel.ReloaderFactory;
import org.zeroturnaround.javarebel.ResourceIntegrationFactory;
import org.zeroturnaround.javarebel.ServletIntegration;
import org.zeroturnaround.javarebel.ServletIntegrationFactory;

import com.liferay.portal.kernel.deploy.hot.HotDeployEvent;

/**
 * 
 * @author Julien Bonjean <julien@bonjean.info>
 * 
 * TODO: do not use a singleton, associate one weak instance per portlet
 * TODO: add support for unregister on portlet undeploy
 * 
 */
public class ResourceReloader implements FileEventListener
{
	private static final Logger log = LoggerFactory.getLogger(ResourceReloader.class.getName());
	private static final String COMMAND = System.getProperty("user.home") + "/.jrebel/lr_post_cmd.sh";
	private static final int CMD_DELAY = 1000;

	// TODO: use a weak instance (WeakUtil.weak)
	private static ResourceReloader instance;

	private final ScheduledExecutorService scheduledThreadPool = Executors.newScheduledThreadPool(1);
	private final HashSet<String> registeredContexts = new HashSet<String>();
	private ScheduledFuture<?> scheduledFuture;

	private static Runnable executor = new Runnable()
	{
		@Override
		public void run()
		{
			log.infoEcho("executing post-reload command");
			try
			{
				Process process = Runtime.getRuntime().exec(COMMAND);
				process.waitFor();
				BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
				String line = "";
				while ((line = bufferedReader.readLine()) != null)
					log.errorEcho(line);
				bufferedReader.close();
			}
			catch (IOException | InterruptedException e)
			{
				log.error(e.getMessage());
			}
		}
	};

	private void execute()
	{
		// this mechanism is used to prevent the command to be executed too many times if multiple resources are
		// modified
		if (scheduledFuture != null)
			scheduledFuture.cancel(false);
		scheduledFuture = scheduledThreadPool.schedule(executor, CMD_DELAY, TimeUnit.MILLISECONDS);
	}

	public ResourceReloader()
	{
		super();

		// monitor portlet classes
		// TODO: move to the plugin preinit
		ReloaderFactory.getInstance().addClassReloadListener(new ClassEventListener()
		{
			@Override
			public int priority()
			{
				return PRIORITY_DEFAULT;
			}

			@Override
			public void onClassEvent(int arg0, Class<?> clazz)
			{
				ClassLoader classLoader = clazz.getClassLoader();
				if (classLoader instanceof WebappClassLoader)
				{
					// ensure this is a context we monitor
					String contextPath = ((WebappClassLoader) classLoader).getContextName();
					if (registeredContexts.contains(contextPath))
						execute();
				}
			}
		});
	}

	public static ResourceReloader getInstance()
	{
		if (instance == null)
			instance = new ResourceReloader();
		return instance;
	}

	public void register(HotDeployEvent hotDeployEvent)
	{
		ServletContext servletContext = hotDeployEvent.getServletContext();
		String servletContextPath = servletContext.getContextPath();

		// store a list of servlet registered contexts
		log.infoEcho("register context " + servletContextPath);
		registeredContexts.add(servletContextPath);

		// monitor portlet resources
		ServletIntegration servletIntegration = ServletIntegrationFactory.getInstance();
		RebelSource[] rebelSources = servletIntegration.getRebelSources((RebelServletContext) servletContext);
		for (RebelSource rebelSource : rebelSources)
			ResourceIntegrationFactory.getInstance().addFileListener(rebelSource.getFile(), this);
	}

	@Override
	public boolean isRecursive()
	{
		return true;
	}

	@Override
	public void onFailure()
	{
	}

	@Override
	public void onFileAdd(File arg0)
	{
		execute();
	}

	@Override
	public void onFileChange(File arg0)
	{
		execute();
	}

	@Override
	public void onFileDirty(File arg0)
	{
	}

	@Override
	public void onFileRemove(File arg0)
	{
		execute();
	}
}
