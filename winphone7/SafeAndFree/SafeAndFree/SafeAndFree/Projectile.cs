using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using SafeAndFree.Data;
using Microsoft.Xna.Framework;
using SafeAndFree.Helpers;
using SafeAndFree.Enumerations;

namespace SafeAndFree
{
    public class Projectile : Actor
    {
        public ProjectileTypes Type { get; private set; }
        public WeaponStats Stats;
        public Vector2 CurrentPoint;
        public Creep TargetCreep;

        public Projectile(WeaponStats stats, Creep targetCreep, Vector2 startPoint, TowerTypes parentTowerType)
        {
            Stats = stats.GetCopy();
            TargetCreep = targetCreep;
            CurrentPoint = startPoint;
            SelectTypeBasedOnTowerType(parentTowerType);
            this.TextureID =  TowerFactory.GetProjectileMediaID(Type);
        }

        private void SelectTypeBasedOnTowerType(TowerTypes type)
        {
            switch(type)
            {
                case TowerTypes.Slow:
                    Type = ProjectileTypes.Slow;
                    break;
                default:
                Type = ProjectileTypes.Normal;
                break;
            }
            //have a switch here at some point
        }

        /// <summary>
        /// Update this Projectile instance.
        /// </summary>
        /// <returns>True if the projectile should be removed.</returns>
        public bool Update()
        {
            bool result;
            if (TargetCreep == null)
            {
                return true;
            }

            CurrentPoint = Calculator.MovementTowardsPoint(CurrentPoint, TargetCreep.CenterPosition, Stats.Speed, out result);

            if (result)
            {
                TargetCreep.TakeHit(this);
            }

            return result;
        }
    }
}
