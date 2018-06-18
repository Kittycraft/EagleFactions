/*
 * Copyright (c) 2016 riebie, Kippers <https://bitbucket.org/Kippers/mcclans-core-sponge>
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package nl.riebie.mcclans.persistence;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.concurrent.ConcurrentLinkedQueue;

import nl.riebie.mcclans.MCClans;
import nl.riebie.mcclans.config.Config;

public class TaskExecutor extends Thread {

    private static TaskExecutor instance;

    private boolean running = false;
    private boolean finished = true;
    private ConcurrentLinkedQueue<MCClansDatabaseTask> queue = new ConcurrentLinkedQueue<MCClansDatabaseTask>();

    protected TaskExecutor() {
    }

    public static TaskExecutor getInstance() {
        if (instance == null) {
            instance = new TaskExecutor();
        }
        return instance;
    }

    public boolean initialize() {
        if (!running && finished) {
            running = true;
            finished = false;
            super.start();
            return true;
        } else {
            MCClans.getPlugin().getLogger().warn("Could not initialize TaskExecutor! Running: " + running + ", finished: " + finished, true);
            return false;
        }
    }

    public boolean enqueue(MCClansDatabaseTask task) {
        return running && queue.add(task);
    }

    public boolean isQueueEmpty() {
        return queue.isEmpty();
    }

    public void terminate() {
        if (!finished) {
            synchronized (this) {
                running = false;
                while (!finished) {
                    try {
                        this.wait();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    @Override
    public void run() {
        while (!finished) {
            MCClansDatabaseTask task = queue.poll();
            if (task == null) {
                if (!running) {
                    synchronized (this) {
                        this.notifyAll();
                        finished = true;
                    }
                }
            } else {
                PreparedStatement query = task.getQuery();
                try {
                    query.execute();
                } catch (SQLException e) {
                    if (Config.getBoolean(Config.DEBUGGING)) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    public boolean isRunning() {
        return running;
    }
}
