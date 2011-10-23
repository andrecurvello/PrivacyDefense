using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using Microsoft.Xna.Framework;

namespace SafeAndFree.Helpers
{
    /// <summary>
    /// Generic match helper methods.
    /// </summary>
    static class Calculator
    {
        /// <summary>
        /// Get the distance between two points.
        /// </summary>
        /// <param name="pos1"></param>
        /// <param name="pos2"></param>
        /// <returns></returns>
        public static double GetDistance(Vector2 pos1, Vector2 pos2)
        {
            return Math.Sqrt(Math.Pow(pos1.X - pos2.X, 2) + Math.Pow(pos1.Y - pos2.Y, 2));
        }

        
        /// <summary>
        /// Gives the movement vector for an object to move towards
        /// the given point.
        /// </summary>
        /// <param name="start">The position to move from.</param>
        /// <param name="end">The position to move to.</param>
        /// <param name="speed">The vector's magnitude.</param>
        /// <param name="Connected">True if the vector </param>
        /// <returns>The movement vector.</returns>
        public static Vector2 MovementTowardsPoint(Vector2 start, Vector2 end, int speed, out bool Connected)
        {
            double totalDistance = GetDistance(start, end);


            //double movementPercent =  totalDistance / (double)speed;
            double movementPercent = 0;
            if (movementPercent >= 1)
            {
                Connected = true;
                return end;
            }
            Connected = false;
            
            double xDiff = Math.Abs(start.X - end.X);
            double yDiff = Math.Abs(start.Y - end.Y);
            double totalDiff = xDiff + yDiff;
            int xPos = start.X > end.X ? -1 : 1;
            int yPos = start.Y > end.Y ? -1 : 1;

            double xRatio = xDiff / yDiff; //x to y ratio
            double xSpeed = (double)speed * (xDiff / totalDiff);
            double ySpeed = (double) speed * (yDiff / totalDiff);

            return new Vector2((float)(start.X + (xSpeed * xPos)), (float)(start.Y + ( ySpeed * yPos)));
        }

        /// <summary>
        /// Not yet the best shootable creep, right now just any creep
        /// returns true if there's a valid creep, creep is the target
        /// </summary>
        /// <param name="targets"></param>
        /// <param name="towerPosition"></param>
        /// <returns></returns>
        public static bool BestShootableCreep(List<Creep> targets, Vector2 towerPosition, int range, out Creep creep)
        {
            creep = null;
            if (targets.Count > 0)
            {
                creep = targets[0];
            }
            return true;
        }
    }
}
